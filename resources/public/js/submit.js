$(document).ready(function() {
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
      // set the stripe token in a hidden input and submit the form.
      $("#stripe-token")
        .val(token.id)
        .parent("form")
        .submit();
    }
  });

  $('#checkout-btn').click(function(e) {
    handler.open();
    e.preventDefault();
  });

  $(window).on('popstate', function() {
    handler.close();
  });
});
