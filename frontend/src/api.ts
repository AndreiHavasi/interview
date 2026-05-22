export interface TopicEntry {
  slug: string;
  title: string;
}

export interface TopicNode {
  id: number;
  slug: string;
  title: string;
  difficulty: number | null;
  children: TopicNode[];
  entries: TopicEntry[];
}

export interface Entry {
  slug: string;
  topicSlug: string;
  title: string;
  bodyMd: string;
  locale: 'en' | 'ro';
  fellBack: boolean;
  difficulty: number | null;
  sources: string[];
  related: string[];
  roCompanies: string[];
  availableLocales: string[];
}

const BASE = '';

export async function fetchTopics(): Promise<TopicNode[]> {
  const r = await fetch(`${BASE}/api/topics`);
  if (!r.ok) throw new Error(`topics ${r.status}`);
  return r.json();
}

export async function fetchEntry(slug: string, locale: 'en' | 'ro'): Promise<Entry> {
  const r = await fetch(`${BASE}/api/topics/by-path/${slug}?locale=${locale}`);
  if (!r.ok) throw new Error(`entry ${r.status}`);
  return r.json();
}

export async function reloadContent(): Promise<{ added: number; updated: number; unchanged: number }> {
  const r = await fetch(`${BASE}/admin/reload-content`, { method: 'POST' });
  if (!r.ok) throw new Error(`reload ${r.status}`);
  return r.json();
}
