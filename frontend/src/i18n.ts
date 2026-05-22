export type Locale = 'en' | 'ro';

const messages = {
  en: {
    appTitle: 'Interview Prep',
    sidebarHeader: 'Topics',
    selectEntry: 'Select a topic on the left to start studying.',
    notFound: 'Entry not found.',
    fellBackBanner: 'Romanian translation not yet written — showing English.',
    reload: 'Reload content',
    reloading: 'Reloading…',
    reloaded: (a: number, u: number) => `Reloaded: ${a} added, ${u} updated`,
    reloadFailed: 'Reload failed',
    sources: 'Sources',
    related: 'Related',
    roCompanies: 'Asked at',
    difficulty: 'Difficulty',
  },
  ro: {
    appTitle: 'Pregătire interviu',
    sidebarHeader: 'Subiecte',
    selectEntry: 'Selectează un subiect din stânga pentru a începe.',
    notFound: 'Articol negăsit.',
    fellBackBanner: 'Traducerea în română lipsește — afișez versiunea în engleză.',
    reload: 'Reîncarcă conținutul',
    reloading: 'Reîncarc…',
    reloaded: (a: number, u: number) => `Reîncărcat: ${a} adăugate, ${u} actualizate`,
    reloadFailed: 'Reîncărcarea a eșuat',
    sources: 'Surse',
    related: 'Înrudite',
    roCompanies: 'Întrebat la',
    difficulty: 'Dificultate',
  },
} as const;

export function useT(locale: Locale) {
  return messages[locale];
}
