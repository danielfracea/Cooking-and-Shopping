import { createI18n } from 'vue-i18n'
import en from './en.js'
import ro from './ro.js'

const savedLocale = localStorage.getItem('locale') || 'en'

const i18n = createI18n({
  legacy: false,
  locale: savedLocale,
  fallbackLocale: 'en',
  messages: { en, ro },
})

export default i18n
