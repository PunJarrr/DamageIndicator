// Joins a local test server as SmokeBot (an operator, see scripts/smoke-test.sh) and checks what a player sees:
//   1. runs /di reload                                        -> expects "Reloaded configuration successfully!"
//   2. hits a pig (blacklisted in the test config)            -> expects no floating text
//   3. hits a cow for 3 damage                                -> expects floating text "-3❤ [7❤]"
//   4. heals the cow with Instant Health                      -> expects floating text "+4❤ [10❤]"
//   5. waits                                                  -> expects all the text to be removed again
// Commands run as a player, so this also works on Folia, whose console can't reach entities.
// Prints PASS/FAIL/SKIP lines. Exit code 0 = pass, 1 = fail, 3 = this Minecraft version isn't supported by Mineflayer.
//
//   node bot.js <port>
const mineflayer = require('mineflayer')
const nbt = require('prismarine-nbt')

const port = Number(process.argv[2] || 25565)
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
  const own = component.text !== undefined ? component.text : (component[''] !== undefined ? component[''] : '')
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

setTimeout(() => finish(1, 'FAIL timed out after 120 seconds'), 120000)

const bot = mineflayer.createBot({ host: '127.0.0.1', port, username: 'SmokeBot', auth: 'offline' })

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
bot.on('kicked', reason => finish(1, `FAIL kicked: ${JSON.stringify(reason)}`))
bot.on('error', error => {
  if (/unsupported|not supported|No data available/i.test(String(error && error.message))) {
    finish(3, `SKIP Mineflayer can't join this version (${error.message})`)
  }
  finish(1, `FAIL ${error && error.stack ? error.stack : error}`)
})

const shown = () => [...texts.entries()].filter(([id]) => !removed.has(id)).map(([, text]) => text)
const newText = (before, wanted) => () => [...texts.entries()].find(([id, text]) => !before.has(id) && text.includes(wanted))

bot.once('spawn', async () => {
  try {
    console.log(`joined as SmokeBot, Minecraft ${bot.version}`)
    await sleep(3000) // let the world settle and the op status arrive
    bot.chat('/gamemode creative')

    bot.chat('/di reload')
    if (!await waitFor(() => chatLines.find(line => line.includes('Reloaded configuration successfully')), 8000)) {
      return finish(1, `FAIL /di reload gave no reply (saw: ${JSON.stringify(chatLines.slice(-5))})`)
    }
    console.log('ok   /di reload')

    bot.chat('/summon pig ~3 ~ ~ {NoAI:1b,Tags:["dibot"]}')
    bot.chat('/summon cow ~-3 ~ ~ {NoAI:1b,Tags:["dibot"]}')
    await sleep(1500)

    let before = new Set(texts.keys())
    bot.chat('/damage @e[type=pig,tag=dibot,limit=1] 1')
    await sleep(2000)
    const pigText = [...texts.keys()].filter(id => !before.has(id)).map(id => texts.get(id))
    if (pigText.length) return finish(1, `FAIL the blacklisted pig got text: ${JSON.stringify(pigText)}`)
    console.log('ok   no text for the blacklisted pig')

    before = new Set(texts.keys())
    bot.chat('/damage @e[type=cow,tag=dibot,limit=1] 3')
    const damage = await waitFor(newText(before, '-3'), 8000)
    if (!damage) return finish(1, `FAIL no damage text (saw: ${JSON.stringify(shown())})`)
    if (damage[1] !== '-3❤ [7❤]') return finish(1, `FAIL unexpected damage text: ${damage[1]}`)
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

    bot.quit()
    finish(0, 'PASS /di reload, blacklist, damage and heal text, and removal work for a player')
  } catch (error) {
    finish(1, `FAIL ${error.stack || error}`)
  }
})
