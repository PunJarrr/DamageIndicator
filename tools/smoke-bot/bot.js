// Joins a local test server as SmokeBot (an operator, see scripts/smoke-test.sh) and checks what a player sees:
//   1. runs /di reload                                        -> expects "Reloaded configuration successfully!"
//   2. hits a pig (blacklisted in the test config)            -> expects no floating text
//   3. hits a cow for 3 damage                                -> expects floating text "-3❤ [7❤]"
//   4. heals the cow with Instant Health                      -> expects floating text "+4❤ [10❤]"
//   5. waits                                                  -> expects all the text to be removed again
//   5b. hurts itself (players aren't blacklisted here)       -> a number is made (execute finds it), but the
//       bot isn't sent it (hide-own-indicators)
//   6. runs /di, then hits the cow for 1                      -> expects "now off", and no floating text
//   7. leaves and joins again, hits the cow for 1             -> still no floating text (the choice was saved)
//   8. runs /di, then hits the cow for 1                      -> expects "now on", and "-1❤ [7❤]"
// With "papi" (the server has PlaceholderAPI, and the damage text ends in %damageindicator_status%):
//   - the /di reload reply ends in "true" (a placeholder in a message, read for the bot)
//   - damage text ends in " ON" (a placeholder in the text, read for the bot, who hits the cow)
//   - /papi parse me %damageindicator_status% says OFF after step 6 and 7, and ON after step 8
// Commands run as a player, so this also works on Folia, whose console can't reach entities.
// Prints PASS/FAIL/SKIP lines. Exit code 0 = pass, 1 = fail, 3 = this Minecraft version isn't supported by Mineflayer.
//
//   node bot.js <port> [papi]
const mineflayer = require('mineflayer')
const nbt = require('prismarine-nbt')

const port = Number(process.argv[2] || 25565)
const papi = process.argv[3] === 'papi'
// What the damage text ends with: the attacker's %damageindicator_status%, when PlaceholderAPI is there
const suffix = papi ? ' ON' : ''
// The bot hits the mobs itself, so placeholders in the damage text are read for the bot
const hit = (mob, amount) => bot.chat(`/damage @e[type=${mob},tag=dibot,limit=1] ${amount} minecraft:player_attack by @s`)
const sleep = ms => new Promise(resolve => setTimeout(resolve, ms))

// Text components arrive as JSON strings (older versions) or NBT (1.20.3+). Reduce them to plain text.
function plainText (component) {
  if (component === undefined || component === null) return ''
  if (typeof component === 'string') {
    try { return plainText(JSON.parse(component)) } catch { return component }
  }
  if (component.type && component.value !== undefined) component = nbt.simplify(component)
  if (typeof component !== 'object') return String(component)
  if (Array.isArray(component)) return component.map(plainText).join('')
  // The game's own replies (like "Test passed") are translation keys: keep the key and its values
  const translated = component.translate !== undefined
    ? [component.translate, ...(component.with || []).map(plainText)].join(' ')
    : ''
  const own = component.text !== undefined ? component.text : (component[''] !== undefined ? component[''] : translated)
  return String(own) + (component.extra || []).map(plainText).join('')
}

const chatLines = []
const texts = new Map() // entity id -> floating text (only text containing ❤, i.e. ours)
const removed = new Set()

async function waitFor (predicate, timeoutMs) {
  const end = Date.now() + timeoutMs
  while (Date.now() < end) {
    const match = predicate()
    if (match) return match
    await sleep(100)
  }
  return null
}

function finish (code, message) {
  console.log(message)
  process.exit(code)
}

setTimeout(() => finish(1, 'FAIL timed out after 150 seconds'), 150000)

let bot
let leaving = false

// Joins the server and resolves once the bot is in the world. Called again to rejoin.
function join () {
  return new Promise(resolve => {
    leaving = false
    bot = mineflayer.createBot({ host: '127.0.0.1', port, username: 'SmokeBot', auth: 'offline' })
    bot._client.on('system_chat', data => chatLines.push(plainText(data.content || data.formattedMessage)))
    bot._client.on('chat', data => chatLines.push(plainText(data.message)))
    // The floating text is entity metadata. Its index differs between versions, so look at every value.
    bot._client.on('entity_metadata', packet => {
      for (const entry of packet.metadata || []) {
        const text = plainText(entry.value)
        if (text.includes('❤')) texts.set(packet.entityId, text)
      }
    })
    for (const name of ['entity_destroy', 'remove_entities']) {
      bot._client.on(name, packet => (packet.entityIds || []).forEach(id => removed.add(id)))
    }
    bot.on('kicked', reason => { if (!leaving) finish(1, `FAIL kicked: ${JSON.stringify(reason)}`) })
    bot.on('error', error => {
      if (leaving) return
      if (/unsupported|not supported|No data available/i.test(String(error && error.message))) {
        finish(3, `SKIP Mineflayer can't join this version (${error.message})`)
      }
      finish(1, `FAIL ${error && error.stack ? error.stack : error}`)
    })
    bot.once('spawn', resolve)
  })
}

async function leave () {
  leaving = true
  bot.quit()
  await sleep(3000) // let the server finish saving the player
}

const shown = () => [...texts.entries()].filter(([id]) => !removed.has(id)).map(([, text]) => text)
const newText = (before, wanted) => () => [...texts.entries()].find(([id, text]) => !before.has(id) && text.includes(wanted))
const newTexts = before => [...texts.keys()].filter(id => !before.has(id)).map(id => texts.get(id))
const chatSince = (count, wanted) => () => chatLines.slice(count).find(line => line.includes(wanted))

// Runs a command and waits for a chat line containing `wanted`
async function command (text, wanted) {
  const count = chatLines.length
  bot.chat(text)
  if (!await waitFor(chatSince(count, wanted), 8000)) {
    finish(1, `FAIL ${text} gave no "${wanted}" reply (saw: ${JSON.stringify(chatLines.slice(count))})`)
  }
  console.log(`ok   ${text}`)
}

// Hits the cow for 1 and expects no floating text, as the numbers are turned off
async function expectNoTextForDamage (why) {
  const before = new Set(texts.keys())
  hit('cow', 1)
  await sleep(2500)
  const seen = newTexts(before)
  if (seen.length) finish(1, `FAIL got text ${why}: ${JSON.stringify(seen)}`)
  console.log(`ok   no text ${why}`)
}

// /papi parse: what %damageindicator_status% is for the bot now
async function expectStatus (wanted) {
  if (!papi) return
  const count = chatLines.length
  bot.chat('/papi parse me %damageindicator_status%')
  if (!await waitFor(() => chatLines.slice(count).find(line => line.trim() === wanted), 8000)) {
    finish(1, `FAIL %damageindicator_status% is not ${wanted} (saw: ${JSON.stringify(chatLines.slice(count))})`)
  }
  console.log(`ok   %damageindicator_status% is ${wanted}`)
}

async function main () {
  await join()
  console.log(`joined as SmokeBot, Minecraft ${bot.version}`)
  await sleep(3000) // let the world settle and the op status arrive
  bot.chat('/gamemode creative')

  await command('/di reload', papi ? 'Reloaded configuration successfully! true' : 'Reloaded configuration successfully')

  bot.chat('/summon pig ~3 ~ ~ {NoAI:1b,Tags:["dibot"]}')
  bot.chat('/summon cow ~-3 ~ ~ {NoAI:1b,Tags:["dibot"]}')
  await sleep(1500)

  let before = new Set(texts.keys())
  hit('pig', 1)
  await sleep(2000)
  const pigText = newTexts(before)
  if (pigText.length) return finish(1, `FAIL the blacklisted pig got text: ${JSON.stringify(pigText)}`)
  console.log('ok   no text for the blacklisted pig')

  before = new Set(texts.keys())
  hit('cow', 3)
  const damage = await waitFor(newText(before, '-3'), 8000)
  if (!damage) return finish(1, `FAIL no damage text (saw: ${JSON.stringify(shown())})`)
  if (damage[1] !== '-3❤ [7❤]' + suffix) return finish(1, `FAIL unexpected damage text: ${damage[1]}`)
  console.log(`ok   ${damage[1]}`)

  before = new Set(texts.keys())
  bot.chat('/effect give @e[type=cow,tag=dibot] instant_health 1 0')
  const heal = await waitFor(newText(before, '+4'), 8000)
  if (!heal) return finish(1, `FAIL no heal text (saw: ${JSON.stringify(shown())})`)
  if (heal[1] !== '+4❤ [10❤]') return finish(1, `FAIL unexpected heal text: ${heal[1]}`)
  console.log(`ok   ${heal[1]}`)

  if (!await waitFor(() => shown().length === 0, 20000)) {
    return finish(1, `FAIL the text was not removed (still showing: ${JSON.stringify(shown())})`)
  }
  console.log('ok   the text was removed again')

  // hide-own-indicators: the bot hurts itself. A number is made (others would see it), but not sent to the bot.
  bot.chat('/gamemode survival')
  await sleep(1000)
  const healthBefore = bot.health
  let lowestHealth = healthBefore // it heals back quickly, so keep the lowest value seen
  const onHealth = () => { lowestHealth = Math.min(lowestHealth, bot.health) }
  bot.on('health', onHealth)
  before = new Set(texts.keys())
  await command('/damage @s 2', 'commands.damage.success')
  await command('/execute if entity @e[type=text_display,distance=..6]', 'commands.execute.conditional.pass')
  await sleep(1500)
  const ownText = newTexts(before)
  if (ownText.length) return finish(1, `FAIL the bot was sent its own number: ${JSON.stringify(ownText)}`)
  bot.removeListener('health', onHealth)
  if (!(lowestHealth < healthBefore)) return finish(1, `FAIL the bot took no damage (health stayed ${healthBefore})`)
  console.log('ok   no number for the bot\'s own damage, though one was made for others')
  bot.chat('/gamemode creative')

  // The cow has 10 health. Two hits of 1 while the numbers are off, so turning them on shows "[7❤]".
  await expectStatus('ON')
  await command('/di', 'now off')
  await expectStatus('OFF')
  await expectNoTextForDamage('after turning numbers off with /di')

  await leave()
  await join()
  console.log('ok   left and joined again')
  await sleep(2000)
  await expectNoTextForDamage('after rejoining (the choice was saved)')
  await expectStatus('OFF')

  await command('/di', 'now on')
  await expectStatus('ON')
  before = new Set(texts.keys())
  hit('cow', 1)
  const again = await waitFor(newText(before, '-1'), 8000)
  if (!again) return finish(1, `FAIL no damage text after turning numbers back on (saw: ${JSON.stringify(shown())})`)
  if (again[1] !== '-1❤ [7❤]' + suffix) return finish(1, `FAIL unexpected damage text: ${again[1]}`)
  console.log(`ok   ${again[1]}`)

  await leave()
  finish(0, 'PASS /di reload, blacklist, damage and heal text, removal, hiding your own number, and turning numbers off and on with /di work for a player'
    + (papi ? ', with PlaceholderAPI placeholders' : ''))
}

main().catch(error => finish(1, `FAIL ${error.stack || error}`))
