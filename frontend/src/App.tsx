import { useEffect, useState } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { Entry, TopicNode, fetchEntry, fetchTopics, reloadContent } from './api';
import { Locale, useT } from './i18n';

function readLocale(): Locale {
  const v = localStorage.getItem('locale');
  return v === 'ro' ? 'ro' : 'en';
}

export default function App() {
  const [locale, setLocale] = useState<Locale>(readLocale());
  const [tree, setTree] = useState<TopicNode[]>([]);
  const [selected, setSelected] = useState<string | null>(null);
  const [entry, setEntry] = useState<Entry | null>(null);
  const [loading, setLoading] = useState(false);
  const [reloadStatus, setReloadStatus] = useState<string | null>(null);
  const t = useT(locale);

  useEffect(() => {
    localStorage.setItem('locale', locale);
  }, [locale]);

  useEffect(() => {
    fetchTopics().then(setTree).catch(console.error);
  }, []);

  useEffect(() => {
    if (!selected) { setEntry(null); return; }
    setLoading(true);
    fetchEntry(selected, locale)
      .then(setEntry)
      .catch(err => { console.error(err); setEntry(null); })
      .finally(() => setLoading(false));
  }, [selected, locale]);

  const onReload = async () => {
    setReloadStatus(t.reloading);
    try {
      const r = await reloadContent();
      setReloadStatus(t.reloaded(r.added, r.updated));
      const next = await fetchTopics();
      setTree(next);
      if (selected) {
        const e = await fetchEntry(selected, locale);
        setEntry(e);
      }
    } catch (e) {
      console.error(e);
      setReloadStatus(t.reloadFailed);
    }
  };

  return (
    <div className="h-full flex flex-col">
      <header className="bg-slate-900 text-white px-6 py-3 flex items-center justify-between shadow">
        <h1 className="text-lg font-semibold">{t.appTitle}</h1>
        <div className="flex items-center gap-3">
          <button
            onClick={onReload}
            className="px-3 py-1 rounded bg-slate-700 hover:bg-slate-600 text-sm"
            title="POST /admin/reload-content"
          >
            {t.reload}
          </button>
          {reloadStatus && <span className="text-xs text-slate-300">{reloadStatus}</span>}
          <div className="flex rounded overflow-hidden border border-slate-600">
            <button
              className={`px-2 py-1 text-sm ${locale === 'en' ? 'bg-slate-100 text-slate-900' : 'bg-slate-800 text-slate-200'}`}
              onClick={() => setLocale('en')}
            >EN</button>
            <button
              className={`px-2 py-1 text-sm ${locale === 'ro' ? 'bg-slate-100 text-slate-900' : 'bg-slate-800 text-slate-200'}`}
              onClick={() => setLocale('ro')}
            >RO</button>
          </div>
        </div>
      </header>

      <div className="flex-1 flex overflow-hidden">
        <aside className="w-72 border-r bg-slate-50 overflow-y-auto p-3">
          <h2 className="text-xs uppercase tracking-wide text-slate-500 mb-2">{t.sidebarHeader}</h2>
          <Tree nodes={tree} selected={selected} onSelect={setSelected} />
        </aside>
        <main className="flex-1 overflow-y-auto p-6 bg-white">
          {loading && <div className="text-slate-500 text-sm">…</div>}
          {!loading && !entry && (
            <div className="text-slate-500">{selected ? t.notFound : t.selectEntry}</div>
          )}
          {!loading && entry && <EntryView entry={entry} t={t} />}
        </main>
      </div>
    </div>
  );
}

function Tree({ nodes, selected, onSelect }: {
  nodes: TopicNode[];
  selected: string | null;
  onSelect: (slug: string) => void;
}) {
  return (
    <ul className="space-y-1">
      {nodes.map(n => (
        <TreeNode key={n.id} node={n} selected={selected} onSelect={onSelect} depth={0} />
      ))}
    </ul>
  );
}

function TreeNode({ node, selected, onSelect, depth }: {
  node: TopicNode;
  selected: string | null;
  onSelect: (slug: string) => void;
  depth: number;
}) {
  const [open, setOpen] = useState(true);
  const hasChildren = node.children.length > 0 || node.entries.length > 0;
  return (
    <li>
      <div
        className="flex items-center cursor-pointer text-sm py-0.5 hover:bg-slate-200 rounded px-1"
        style={{ paddingLeft: depth * 12 }}
        onClick={() => setOpen(o => !o)}
      >
        {hasChildren ? (open ? '▾' : '▸') : <span className="inline-block w-3" />}
        <span className="ml-1 font-medium text-slate-800">{node.title}</span>
      </div>
      {open && (
        <ul>
          {node.children.map(c => (
            <TreeNode key={c.id} node={c} selected={selected} onSelect={onSelect} depth={depth + 1} />
          ))}
          {node.entries.map(e => (
            <li
              key={e.slug}
              onClick={() => onSelect(e.slug)}
              className={`cursor-pointer text-sm py-0.5 px-1 rounded ${selected === e.slug ? 'bg-slate-300 text-slate-900' : 'hover:bg-slate-200 text-slate-700'}`}
              style={{ paddingLeft: (depth + 1) * 12 + 14 }}
            >
              {e.title}
            </li>
          ))}
        </ul>
      )}
    </li>
  );
}

function EntryView({ entry, t }: { entry: Entry; t: ReturnType<typeof useT> }) {
  return (
    <article className="max-w-3xl mx-auto">
      {entry.fellBack && (
        <div className="mb-4 px-3 py-2 rounded bg-amber-100 text-amber-900 text-sm border border-amber-200">
          {t.fellBackBanner}
        </div>
      )}
      <div className="prose">
        <ReactMarkdown remarkPlugins={[remarkGfm]}>{entry.bodyMd}</ReactMarkdown>
      </div>
      <Sidebar entry={entry} t={t} />
    </article>
  );
}

function Sidebar({ entry, t }: { entry: Entry; t: ReturnType<typeof useT> }) {
  const has = entry.sources.length || entry.related.length || entry.roCompanies.length || entry.difficulty;
  if (!has) return null;
  return (
    <div className="mt-8 pt-4 border-t text-sm text-slate-600 space-y-2">
      {entry.difficulty != null && <div><strong>{t.difficulty}:</strong> {entry.difficulty}/5</div>}
      {entry.roCompanies.length > 0 && (
        <div><strong>{t.roCompanies}:</strong> {entry.roCompanies.join(', ')}</div>
      )}
      {entry.related.length > 0 && (
        <div><strong>{t.related}:</strong> {entry.related.join(', ')}</div>
      )}
      {entry.sources.length > 0 && (
        <div>
          <strong>{t.sources}:</strong>
          <ul className="list-disc ml-5">
            {entry.sources.map(s => (
              <li key={s}><a href={s} target="_blank" rel="noreferrer" className="text-blue-600 underline">{s}</a></li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
