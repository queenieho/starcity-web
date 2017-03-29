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
});
