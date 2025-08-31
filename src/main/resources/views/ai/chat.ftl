<!doctype html>
<html>
<head>
    <meta charset="utf-8"/>
    <title>AI Chat</title>
    <style>
        :root {
            --bg: #f6f7f9;
            --fg: #0b1220;
            --muted: #6b7280;
            --user: #0a58ca;
            --bot: #198754;
            --tool: #6f42c1;
            --bubble: #fff;
            --bubble-user: #e8f0ff;
            --bubble-bot: #eaf7f0;
            --code-bg: #0f172a;
            --code-fg: #e5e7eb;
        }
        body { font-family: system-ui, Segoe UI, Roboto, sans-serif; background: var(--bg); color: var(--fg); margin: 2rem; }
        h1 { margin: 0 0 1rem 0; font-size: 1.25rem; }
        #log { border: 1px solid #e5e7eb; background: #fff; border-radius: 12px; padding: 1rem; height: 420px; overflow:auto; margin-bottom: 1rem; }
        .row { display: flex; margin: .5rem 0; gap: .5rem; }
        .row.user { justify-content: flex-end; }
        .row.bot  { justify-content: flex-start; }
        .row.tool { justify-content: center; }
        .avatar { width: 28px; height: 28px; border-radius: 50%; display:flex; align-items:center; justify-content:center; font-size: .75rem; color:#fff; }
        .avatar.user { background: var(--user); }
        .avatar.bot  { background: var(--bot); }
        .avatar.tool { background: var(--tool); }
        .bubble { max-width: 72%; background: var(--bubble); border-radius: 12px; padding: .75rem .9rem; box-shadow: 0 1px 2px rgba(0,0,0,.05); white-space: normal; overflow-wrap: anywhere; }
        .user .bubble { background: var(--bubble-user); }
        .bot  .bubble { background: var(--bubble-bot); }
        .tool .bubble { font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace; font-size: .9rem; }
        .sys { color: var(--muted); margin-bottom: .75rem; }
        form { display: flex; gap: .5rem; }
        input#msg { flex: 1; padding: .7rem .9rem; border-radius: 10px; border: 1px solid #d1d5db; font-size: 1rem; }
        button { padding: .7rem 1rem; border-radius: 10px; border: 1px solid #d1d5db; background: #fff; cursor: pointer; }
        /* Markdown-ish */
        .bubble h1,.bubble h2,.bubble h3 { margin: .3rem 0 .4rem; }
        .bubble ul { margin: .25rem 0 .5rem 1.2rem; padding: 0; }
        .bubble li { margin: .2rem 0; }
        .bubble code:not(pre code) { background: #f1f5f9; padding: .1rem .3rem; border-radius: 4px; font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace; }
        .bubble pre { background: var(--code-bg); color: var(--code-fg); padding: .75rem; border-radius: 8px; overflow:auto; }
        .bubble pre code { background: transparent; color: inherit; }
        a { color: inherit; text-decoration: underline; text-underline-offset: 2px; }
    </style>
</head>
<body>
<h1>AI Chat</h1>

<p class="sys">Available tools:
    <#if tools?has_content>
        <code>${tools?join(", ")}</code>
    <#else>
        <em>none registered</em>
    </#if>
</p>

<div id="log"></div>

<form id="f">
    <input id="msg" type="text" placeholder="Say something..." />
    <button>Send</button>
</form>

<#noparse>
<script>
    const log = document.getElementById('log');
    const f = document.getElementById('f');
    const msg = document.getElementById('msg');

    // --- minimal markdown renderer (safe-ish) ---
    function escapeHtml(s) {
        return s.replace(/[&<>]/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;'}[c]));
    }
    function linkify(s) {
        return s.replace(/\bhttps?:\/\/[^\s)]+/g, u => `<a href="${u}" target="_blank" rel="noopener">${u}</a>`);
    }
    function renderMarkdown(text) {
        // handle fenced code blocks first
        let html = "";
        const parts = String(text).split(/```/);
        for (let i = 0; i < parts.length; i++) {
            if (i % 2 === 1) { // code block
                const block = parts[i].replace(/^\w+\n/, ''); // drop optional lang
                html += `<pre><code>${escapeHtml(block)}</code></pre>`;
            } else {
                // inline formatting for this chunk
                let t = escapeHtml(parts[i]);

                // bold **text**
                t = t.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
                // italic *text* (simple)
                t = t.replace(/(^|[\s(])\*(.+?)\*/g, '$1<em>$2</em>');
                // inline code `x`
                t = t.replace(/`([^`]+)`/g, '<code>$1</code>');

                // basic unordered lists: lines starting with - or *
                const lines = t.split(/\n/);
                let inList = false;
                for (let j = 0; j < lines.length; j++) {
                    const m = lines[j].match(/^\s*[-*]\s+(.+)/);
                    if (m) {
                        if (!inList) { lines[j] = '<ul><li>'+ m[1] +'</li>'; inList = true; }
                        else { lines[j] = '<li>'+ m[1] +'</li>'; }
                        // close list if next line not a bullet
                        const next = lines[j+1] || '';
                        if (!/^\s*[-*]\s+/.test(next)) { lines[j] += '</ul>'; inList = false; }
                    } else {
                        // paragraphs: turn lone newlines into <br>
                        lines[j] = lines[j].replace(/\n/g, '<br>');
                    }
                }
                t = lines.join('\n');

                // headings (start of line)
                t = t.replace(/^(#{1,3})\s*(.+)$/gm, (_, h, txt) => `<h${h.length}>${txt}</h${h.length}>`);

                // linkify URLs
                t = linkify(t);

                // final paragraph-ish handling
                html += t.replace(/\n{2,}/g, '<br><br>');
            }
        }
        return html;
    }

    function bubble(role, html) {
        const row = document.createElement('div');
        row.className = 'row ' + role;

        const av = document.createElement('div');
        av.className = 'avatar ' + role;
        av.textContent = role === 'user' ? 'U' : (role === 'bot' ? 'A' : 'T');

        const b = document.createElement('div');
        b.className = 'bubble';
        b.innerHTML = html;

        if (role === 'user') { row.appendChild(b); row.appendChild(av); }
        else if (role === 'bot') { row.appendChild(av); row.appendChild(b); }
        else { row.appendChild(b); } // tool/system

        log.appendChild(row);
        log.scrollTop = log.scrollHeight;
    }

    function line(role, text, markdown = false) {
        const html = markdown ? renderMarkdown(text) : escapeHtml(text);
        bubble(role, html);
    }

    f.addEventListener('submit', async (e) => {
        e.preventDefault();
        const text = msg.value.trim();
        if (!text) return;
        line('user', text, false);
        msg.value = '';
        try {
            const res = await fetch('./send', {
                method: 'POST',
                headers: {'Content-Type':'application/json'},
                body: JSON.stringify({ message: text })
            });
            const json = await res.json();
            line('bot', json.reply || '(no reply)', true); // render markdown
        } catch (err) {
            line('tool', 'Error: ' + err, false);
        }
    });
</script>
</#noparse>
</body>
</html>
