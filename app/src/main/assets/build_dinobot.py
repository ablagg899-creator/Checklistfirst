import base64,gzip,pathlib
root=pathlib.Path(__file__).parent
parts=[]
for p in sorted(root.glob('dinobot_html_part*.b64')):
    parts.append(p.read_text().strip())
data=base64.b64decode(''.join(parts))
html=gzip.decompress(data).decode('utf-8')
patch='''<style id="android-layout-fix-v3">
html,body{width:100%;height:100%;margin:0;padding:0;overflow:hidden;overscroll-behavior:none;-webkit-user-select:none;user-select:none;-webkit-tap-highlight-color:transparent;touch-action:manipulation}
#game{width:100%;height:100%;min-height:100%;overflow:hidden;position:relative;background-color:#06131d;background-image:linear-gradient(180deg,rgba(4,18,29,.20),rgba(1,10,18,.78)),radial-gradient(circle at 50% 18%,rgba(39,205,255,.16),transparent 34%),radial-gradient(circle at 82% 72%,rgba(130,55,255,.12),transparent 30%),url("background.jpg");background-position:center;background-size:cover;background-repeat:no-repeat}
button{touch-action:manipulation;-webkit-tap-highlight-color:transparent}
@media(max-width:900px){
 .logo{left:50%!important;top:66px!important;transform:translateX(-50%)!important;width:calc(100% - 20px)!important;max-width:430px!important;padding:8px 10px!important;font-size:25px!important;line-height:.9!important}
 .logo b{font-size:20px!important}.logo small{font-size:10px!important;margin-top:4px!important}
 .resources{left:6px!important;right:6px!important;top:6px!important;transform:none!important;justify-content:space-between!important;gap:4px!important;width:auto!important}
 .res{flex:1 1 0!important;min-width:0!important;width:auto!important;padding:6px 2px!important;font-size:13px!important;border-width:3px!important;border-radius:11px!important;white-space:nowrap!important;overflow:hidden!important}
 .res small{font-size:9px!important}
 .topbuttons,.boosts,.world,.featureDock,.eventMeter{display:none!important}
 .side{left:8px!important;right:8px!important;top:185px!important;bottom:145px!important;width:auto!important;height:auto!important;max-height:none!important;display:grid!important;grid-template-columns:repeat(2,minmax(0,1fr))!important;grid-template-rows:repeat(5,48px)!important;grid-auto-flow:row!important;gap:7px!important;overflow:visible!important;padding:0!important}
 .sidebtn{width:100%!important;height:48px!important;min-height:48px!important;flex:none!important;padding:0 8px!important;font-size:13px!important;border-width:3px!important;border-radius:11px!important;text-align:left!important;white-space:nowrap!important;overflow:hidden!important}
 .sidebtn span{font-size:21px!important;margin-right:5px!important}
 #progressDeck,#eraBadge{display:none!important}
 .comboBox{left:50%!important;bottom:75px!important;width:calc(100% - 32px)!important;max-width:430px!important;text-align:center!important;font-size:12px!important;padding:5px 8px!important;white-space:nowrap!important}
 #tap{left:50%!important;bottom:7px!important;width:min(370px,calc(100% - 28px))!important;height:60px!important;font-size:24px!important;border-width:4px!important;border-radius:17px!important}
 #overlay{padding:8px!important}
 #panel{width:calc(100% - 16px)!important;max-width:none!important;max-height:calc(100% - 16px)!important;padding:8px!important}
 .grid{grid-template-columns:1fr 1fr!important;gap:7px!important}.card{padding:7px!important}.icon{font-size:32px!important}.card h3{font-size:14px!important}.desc{font-size:10px!important;min-height:28px!important}.buy{padding:7px!important;font-size:11px!important}
 #toast{top:14%!important;width:calc(100% - 32px)!important;text-align:center!important;font-size:17px!important;padding:9px 12px!important}
 #eventCard{top:18%!important;width:calc(100% - 28px)!important;padding:10px!important}
 #evolutionBanner{width:calc(100% - 28px)!important;font-size:22px!important;padding:12px!important}
}
@media(max-width:380px){
 .logo{top:63px!important;font-size:22px!important}.logo b{font-size:18px!important}
 .side{top:178px!important;bottom:137px!important;grid-template-rows:repeat(5,43px)!important;gap:5px!important}
 .sidebtn{height:43px!important;min-height:43px!important;font-size:11px!important;padding:0 6px!important}.sidebtn span{font-size:18px!important;margin-right:4px!important}
 #tap{height:56px!important;font-size:21px!important}.comboBox{bottom:67px!important;font-size:11px!important}
}
</style>
<script>(function(){function fit(){var g=document.getElementById('game');if(!g)return;g.style.width=window.innerWidth+'px';g.style.height=window.innerHeight+'px'}window.addEventListener('resize',fit);window.addEventListener('orientationchange',function(){setTimeout(fit,200)});fit()})();</script>'''
if 'android-layout-fix-v3' not in html:
    html=html.replace('</head>',patch+'</head>',1)
(root/'index.html').write_text(html,encoding='utf-8')
print('DinoBot HTML reconstructed with Android v3 layout/background fix:',len(html),'bytes')
