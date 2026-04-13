import { createI18n } from 'vue-i18n'
import en from './en.js'
import ro from './ro.js'
import fr from './fr.js'
import es from './es.js'
import de from './de.js'

const savedLocale = localStorage.getItem('locale') || 'en'

const i18n = createI18n({
  legacy: false,
  locale: savedLocale,
  fallbackLocale: 'en',
  messages: { en, ro, fr, es, de },
})

export default i18n
