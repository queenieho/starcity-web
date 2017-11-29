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
    var modalOpeners = $('a[data-modal]'),
        modalClosers = $('.modal-close-btn'),
        modals       = $('.modal-overlay');

    function showModal(id) {
      var modal = document.getElementById(id);
      $(modal).addClass('open');
      $('body').on('keydown.modal', function(e) {
        if (e.which === 27 ) hideModals();
      });
      $('body').on('click.modal', function(e) {
        var clicked = $(e.target);
        if (clicked.is('.modal-overlay')) hideModals();
      });
    }

    function hideModals() {
      modals.removeClass('open');
      $('body').off('keydown.modal');
      $('body').off('click.modal');
    }

    modalOpeners.click( function(e) {
      var modalId = $(this).attr('data-modal');
      showModal( modalId );
    });

    modalClosers.click( hideModals );

    window.goToSubscribe = function() {
      hideModals();
      $('#subscribe').focus();
    };

    $('.single-item').slick({
        dots: true,
        arrows: false,
        mobileFirst: true,
        responsive: [{
            breakpoint: 425,
            settings: {
                arrows: true
            }
        }]
    });

});
