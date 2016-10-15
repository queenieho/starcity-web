// From http://photoswipe.com/documentation/getting-started.html
$(document).ready(function() {
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

  function openGallery(e) {
    var roomNum = $(e.currentTarget).data().roomNum;
    var pswpElement = document.querySelectorAll('.pswp')[0];

    var options = {
      index: roomNumToIndex(roomNum) || 0,
      bgOpacity: 0.9,
      shareEl: false
    };

    var gallery = new PhotoSwipe(pswpElement, PhotoSwipeUI_Default, items, options);

    gallery.init();
  }

  $("#rooms .opens-gallery").click(openGallery);

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
});
