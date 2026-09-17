import base64,gzip,pathlib
root=pathlib.Path(__file__).parent
parts=[]
for p in sorted(root.glob('dinobot_html_part*.b64')):
    parts.append(p.read_text().strip())
data=base64.b64decode(''.join(parts))
html=gzip.decompress(data).decode('utf-8')
# Android WebView layout/touch patch: preserve the game's responsive UI while preventing
# browser-style scrolling/zooming and accounting for modern phone safe areas.
patch='''<style id="android-layout-fix">html,body{width:100%;height:100%;min-height:100%;overflow:hidden;overscroll-behavior:none;-webkit-user-select:none;user-select:none;-webkit-tap-highlight-color:transparent;touch-action:manipulation}#game{width:100vw;height:100dvh;min-height:100vh;overflow:hidden;padding-top:env(safe-area-inset-top);padding-bottom:env(safe-area-inset-bottom);padding-left:env(safe-area-inset-left);padding-right:env(safe-area-inset-right)}button{touch-action:manipulation;-webkit-tap-highlight-color:transparent}@media(max-width:600px){body{font-size:clamp(12px,3.2vw,18px)}}</style><script>document.addEventListener('touchmove',function(e){if(e.target===document.body)e.preventDefault()},{passive:false});</script>'''
if '</head>' in html and 'android-layout-fix' not in html:
    html=html.replace('</head>',patch+'</head>',1)
(root/'index.html').write_text(html,encoding='utf-8')
print('DinoBot HTML reconstructed and Android layout patched:',len(html),'bytes')
