import http from 'node:http';
const KEY = process.env.ANTHROPIC_API_KEY, TOKEN = process.env.APP_TOKEN || '';
const MODEL = process.env.MODEL || 'claude-haiku-5-5';
const OUT = ' أعد النص الناتج فقط دون أي شرح أو علامات اقتباس.';
const P = {
  spell: 'صحّح الإملاء والنحو في النص مع إضافة التشكيل الكامل، وحافظ على المعنى واللهجة.' + OUT,
  formal: 'أعد صياغة النص بأسلوب رسمي مهذب بنفس اللغة.' + OUT,
  friendly: 'أعد صياغة النص بأسلوب ودي ودافئ بنفس اللغة، ويمكنك إضافة إيموجي مناسب.' + OUT,
  translate: 'إن كان النص عربياً فترجمه إلى الإنجليزية، وإن كان بغيره فترجمه إلى العربية.' + OUT,
  reply: 'هذه رسالة وصلتني. اكتب رداً قصيراً ومناسباً بنفس لغة ولهجة المرسل.' + OUT,
};
http.createServer(async (req, res) => {
  if (req.method !== 'POST' || req.url !== '/ai') return res.writeHead(404).end();
  if (TOKEN && req.headers['x-app-token'] !== TOKEN) return res.writeHead(401).end();
  let body = '';
  for await (const c of req) { body += c; if (body.length > 20000) return res.writeHead(413).end(); }
  try {
    const { mode, text } = JSON.parse(body);
    if (!P[mode] || typeof text !== 'string') return res.writeHead(400).end();
    const r = await fetch('https://api.anthropic.com/v1/messages', {
      method: 'POST',
      headers: { 'content-type': 'application/json', 'x-api-key': KEY, 'anthropic-version': '2023-06-01' },
      body: JSON.stringify({ model: MODEL, max_tokens: 600, system: P[mode], messages: [{ role: 'user', content: text }] }),
    });
    const j = await r.json();
    if (!r.ok) throw new Error(JSON.stringify(j));
    res.writeHead(200, { 'content-type': 'application/json' })
       .end(JSON.stringify({ result: j.content.map(x => x.text || '').join('').trim() }));
  } catch (e) { console.error(e); res.writeHead(500).end(); }
}).listen(process.env.PORT || 8787);
