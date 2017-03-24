$(document).ready(function() {
    var $referral = $("select[name='referral']");
    var $referral_other = $("#referral-other");

    // For rare cases when the page is loaded with other already selected (e.g.
    // back/forward pressed, respectively)
    updateReferralOther($referral.val());

    // Hide/show the "other" input if "other" is selected
    $referral.on('change', function(e) {
        updateReferralOther(e.target.value);
    });

    function updateReferralOther(v) {
        if (v === "other") {
            $referral_other.removeClass("dn").attr("required", true);
        } else {
            $referral_other.addClass("dn").attr("required", false);;
        }
    }
});
