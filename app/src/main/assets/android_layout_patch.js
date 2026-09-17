(function(){
  function fitGame(){
    var g=document.getElementById('game'); if(!g) return;
    var vw=Math.max(document.documentElement.clientWidth,1), vh=Math.max(document.documentElement.clientHeight,1);
    var r=g.getBoundingClientRect(), w=g.scrollWidth||r.width, h=g.scrollHeight||r.height;
    if(w>vw*1.03 || h>vh*1.03){
      var s=Math.min(vw/w,vh/h,1);
      if(s<0.995){g.style.transformOrigin='top left';g.style.transform='scale('+s+')';g.style.width=(vw/s)+'px';g.style.height=(vh/s)+'px';}
      else g.style.transform='';
    }
  }
  window.addEventListener('resize',fitGame,{passive:true});
  window.addEventListener('orientationchange',function(){setTimeout(fitGame,250)},{passive:true});
  document.addEventListener('DOMContentLoaded',function(){setTimeout(fitGame,50);setTimeout(fitGame,500)});
  document.addEventListener('touchmove',function(e){if(e.target===document.body)e.preventDefault()},{passive:false});
})();
