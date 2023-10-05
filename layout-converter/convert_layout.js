
const fs = require('fs')
const yaml = require('js-yaml')

const COMPAT_CHO = 'ㄱㄲㄳㄴㄵㄶㄷㄸㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅃㅄㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ'
const COMPAT_JUNG = 'ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ'

const CONVERT_CHO = 'ᄀᄁ ᄂ  ᄃᄄᄅ       ᄆᄇᄈ ᄉᄊᄋᄌᄍᄎᄏᄐᄑᄒ'
const CONVERT_JUNG = 'ᅡᅢᅣᅤᅥᅦᅧᅨᅩᅪᅫᅬᅭᅮᅯᅰᅱᅲᅳᅴᅵ'
const CONVERT_JONG = 'ᆨᆩᆪᆫᆬᆭᆮ ᆯᆰᆱᆲᆳᆴᆵᆶᆷᆸ ᆹᆺᆻᆼᆽ ᆾᆿᇀᇁᇂ'

const convert = (code) => {
    const char = (typeof code === 'number') ? String.fromCharCode(code) : code
    if(char == ' ') return char
    else if(CONVERT_CHO.includes(char)) {
        return COMPAT_CHO[CONVERT_CHO.indexOf(char)] + '_'
    } else if(CONVERT_JONG.includes(char)) {
        return '_' + COMPAT_CHO[CONVERT_JONG.indexOf(char)]
    } else if(CONVERT_JUNG.includes(char)) {
        return '_' + COMPAT_JUNG[CONVERT_JUNG.indexOf(char)] + '_'
    } else {
        return char
    }
}

const content = fs.readFileSync(process.argv[2] || 'input.yaml', 'utf8').split('\n')
const topRow = content.slice(0, 1)
const input = yaml.load(content.slice(1).join('\n'))
const data = Object.entries(input.map).map(([code, item]) => {
    const base = convert(item.base)
    const shift = convert(item.shift)
    return [code, {base, shift}]
})
const result = {
    map: Object.fromEntries(data)
}
const options = {
    indent: 2,
    forceQuotes: true,
}
const output = topRow.join('\n') + '\n' + yaml.dump(result, options)
fs.writeFileSync(process.argv[3] || 'output.yaml', output)
