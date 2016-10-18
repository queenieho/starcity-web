// From http://photoswipe.com/documentation/getting-started.html
$(document).ready(function() {
  // Mission Community page-specific JS
  // NOTE: May need a unique id for Mission to disambiguate from other
  // pages with .opens-gallery
  var _mission = mission();
  $("#rooms .opens-gallery").click(_mission.openGallery);

  var _navbar = navbar();
  $("#nav-toggle").click(_navbar.click);
});

function navbar() {
  var isActive = false;
  var instance = {};

  instance.click = function(e) {
    var $elt = $(e.currentTarget);
    $elt
      .toggleClass("is-active") // set button to active
      .siblings(".nav-menu")
      .toggleClass("is-active");

    var $nav = $elt.closest(".nav");
    if (isActive) {             // remove
      $nav.css({"background-color": ""});
    } else {
      $nav.css("background-color", $nav.closest(".hero").css("background-color"));
    }
    isActive = !isActive;
  };

  return instance;
}

function mission() {
  var instance = {};
  var items = [
    roomImage(1),
    roomImage(6, true),
    roomImage(7, true),
    roomImage(8),
    roomImage(9),
    roomImage(13),
    roomImage(12, true),
    roomImage(14, true),
    roomImage(15),
    roomImage(16),
    roomImage(17, true),
    roomImage(18),
    roomImage(19, true),
    roomImage(20)
  ];

  function roomImage(imgNum, isPortrait) {
    var h = 1330;
    var w = 2000;
    return {
      src: '/assets/img/mission/rooms/large/room-' + imgNum + '.jpg',
      msrc: '/assets/img/mission/rooms/small/room-' + imgNum + '.jpg',
      w: isPortrait ? h : w,
      h: isPortrait ? w : h
    };
  }

  function roomNumToIndex(roomNum) {
    var map = {
      '13': 5,
      '8': 3,
      '18': 11
    };
    return map[roomNum];
  }

  instance.openGallery = function(e) {
    var roomNum = $(e.currentTarget).data().roomNum;
    var pswpElement = document.querySelectorAll('.pswp')[0];

    var options = {
      index: roomNumToIndex(roomNum) || 0,
      bgOpacity: 0.9,
      shareEl: false
    };

    var gallery = new PhotoSwipe(pswpElement, PhotoSwipeUI_Default, items, options);

    gallery.init();
  };

  return instance;
}
