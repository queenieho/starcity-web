$(document).ready(function() {
  var resizeHeroTitle = wrapFlowText($("#hero-title"), 12, 80);
  var resizeHeroDesc = wrapFlowText($("#hero-description"), 18, 60);

  function setDimensions() {
    var h = $(window).height();
    if (h > 550) {
      $("#hero-wrapper").css("height", h + "px");
    }
    resizeHeroTitle();
    resizeHeroDesc();
  }

  function wrapFlowText(el, factor, max) {
    var currSize = 0;
    return function() {
      var n = Math.floor(el.width() / factor);
      n = Math.min(n, max);
      currSize !== n && (el.css("font-size", n + "px"),
                         currSize = n);
    };
  }

  // resize hero image & text at initialization and on window resize
  setDimensions();

  $(window).resize(function() {
    setDimensions();
  });

  // activate side menu
  $(".button-collapse").sideNav();
});
