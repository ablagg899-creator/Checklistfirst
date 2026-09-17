import base64,gzip,pathlib
root=pathlib.Path(__file__).parent
parts=[]
for p in sorted(root.glob('dinobot_html_part*.b64')):
    parts.append(p.read_text().strip())
data=base64.b64decode(''.join(parts))
html=gzip.decompress(data).decode('utf-8')
patch='''<style id="android-layout-fix-v2">
html,body{width:100%;height:100%;margin:0;overflow:hidden;overscroll-behavior:none;-webkit-user-select:none;user-select:none;-webkit-tap-highlight-color:transparent;touch-action:manipulation}
#game{width:100%;height:100%;min-height:100%;overflow:hidden;position:relative}
button{touch-action:manipulation;-webkit-tap-highlight-color:transparent}
@media(max-width:900px){
 .logo{left:50%!important;top:72px!important;transform:translateX(-50%)!important;width:calc(100vw - 24px)!important;max-width:420px!important;padding:8px 10px!important;font-size:25px!important;line-height:.9!important;border-width:3px!important}
 .logo b{font-size:20px!important}.logo small{font-size:10px!important;margin-top:4px!important}
 .resources{left:8px!important;right:8px!important;top:8px!important;transform:none!important;justify-content:space-between!important;gap:5px!important;width:auto!important}
 .res{flex:1 1 0!important;min-width:0!important;width:auto!important;padding:6px 3px!important;font-size:13px!important;border-width:3px!important;border-radius:12px!important;white-space:nowrap!important}
 .res small{font-size:10px!important}
 .topbuttons,.boosts,.world,.featureDock,.eventMeter{display:none!important}
 .side{left:8px!important;right:8px!important;top:205px!important;bottom:155px!important;width:auto!important;height:auto!important;max-height:none!important;display:grid!important;grid-template-columns:1fr 1fr!important;grid-auto-rows:48px!important;gap:7px!important;overflow:hidden!important;padding:0!important}
 .sidebtn{width:100%!important;height:48px!important;min-height:48px!important;flex:none!important;padding:0 8px!important;font-size:13px!important;border-width:3px!important;border-radius:11px!important;text-align:left!important;white-space:nowrap!important}
 .sidebtn span{font-size:21px!important;margin-right:5px!important}
 #progressDeck{left:8px!important;right:8px!important;top:auto!important;bottom:112px!important;width:auto!important;font-size:10px!important;padding:5px 8px!important}
 .goal{left:8px!important;right:8px!important;bottom:112px!important;width:auto!important;padding:5px!important;font-size:10px!important;display:none!important}
 .comboBox{left:50%!important;bottom:78px!important;width:calc(100vw - 40px)!important;max-width:420px!important;text-align:center!important;font-size:12px!important;padding:5px 8px!important;white-space:nowrap!important}
 #tap{left:50%!important;bottom:8px!important;width:min(360px,calc(100vw - 32px))!important;height:62px!important;font-size:24px!important;border-width:4px!important;border-radius:17px!important}
 #overlay{padding:8px!important}
 #panel{width:calc(100vw - 16px)!important;max-width:none!important;max-height:calc(100vh - 16px)!important;padding:8px!important}
 .grid{grid-template-columns:1fr 1fr!important;gap:7px!important}.card{padding:7px!important}.icon{font-size:32px!important}.card h3{font-size:14px!important}.desc{font-size:10px!important;min-height:28px!important}.buy{padding:7px!important;font-size:11px!important}
 #toast{top:14%!important;width:calc(100vw - 32px)!important;text-align:center!important;font-size:17px!important;padding:9px 12px!important}
 #eventCard{top:18%!important;width:calc(100vw - 28px)!important;padding:10px!important}
 #evolutionBanner{width:calc(100vw - 28px)!important;font-size:22px!important;padding:12px!important}
}
@media(max-width:380px){
 .logo{top:68px!important}.side{top:198px!important;bottom:150px!important;grid-auto-rows:44px!important;gap:5px!important}.sidebtn{height:44px!important;min-height:44px!important;font-size:11px!important}.sidebtn span{font-size:18px!important}
 #tap{height:58px!important;font-size:22px!important}.comboBox{bottom:71px!important;font-size:11px!important}
}
</style>
<script>(function(){function fit(){var g=document.getElementById('game');if(g){g.style.height=window.innerHeight+'px';g.style.width=window.innerWidth+'px';}}window.addEventListener('resize',fit);window.addEventListener('orientationchange',function(){setTimeout(fit,150)});fit();})();</script>'''
if 'android-layout-fix-v2' not in html:
    html=html.replace('</head>',patch+'</head>',1)
(root/'index.html').write_text(html,encoding='utf-8')
print('DinoBot HTML reconstructed and Android portrait layout patched:',len(html),'bytes')
