$(document).ready(function() {
    // Add User Agent
    var doc = document.documentElement;
    doc.setAttribute('data-useragent', navigator.userAgent);


    // Hamburger Icon
    $('.menu-button').on('click', function() {
        $(this).toggleClass('active');
        $('.menu').toggleClass('active');
    });

    // Home Page Header Scroll
    $(window).scroll(function() {
        var scroll = $(window).scrollTop();
        if (scroll >= 900) {
            $("#header-scroll").addClass("fadeIn");
        } else {
            $("#header-scroll").removeClass("fadeIn");
        }
    });


    // Interior Page Header Scroll
    $(window).scroll(function() {
        var scroll = $(window).scrollTop();
        if (scroll >= 400) {
            $("#header-interior-scroll").removeClass("mt4-l w-90-l");
        } else {
            $("#header-interior-scroll").addClass("mt4-l w-90-l");
        }
    });

    log = console.log.bind(console);

    // Modals
    let modalOpeners = $('a[data-modal]'),
        modalClosers = $('.modal-close-btn'),
        modals       = $('.modal-overlay');

    function showModal(id) {
      let modal = document.getElementById(id);
      $(modal).addClass('open');
    }

    function hideModals() {
      modals.removeClass('open');
    }

    modalOpeners.click( function(e) {
      let modalId = $(this).attr('data-modal');
      showModal( modalId );
    });

    modalClosers.click( hideModals );

    window.hideModals = hideModals;
});
