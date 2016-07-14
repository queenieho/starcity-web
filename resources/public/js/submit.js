$(document).ready(function() {
  var receiveCopyCheckbox = $('#receive-copy');
  var paymentSent = false;
  var handler = StripeCheckout.configure({
    name: "Starcity",
    description: "Member Application",
    amount: 2000,
    zipCode: true,
    email: stripe.email,
    allowRememberMe: true,
    key: stripe.key,
    locale: 'auto',
    token: function(token) {
      paymentSent = true;
      // set the stripe token in a hidden input and submit the form.
      $("#stripe-token")
        .val(token.id)
        .parent("form")
        .submit();
    }
  });

  $("form").validate({
    submitHandler: function(form) {
      if (!paymentSent) {
        handler.open();
      } else {
        form.submit();
      }
    }
  });

  $('#background-permission').click(function(e) {
    receiveCopyCheckbox.fadeToggle();
  });

  $(window).on('popstate', function() {
    handler.close();
  });
});
