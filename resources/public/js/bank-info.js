$(document).ready(function() {
  var $form = $("form");
  var $submit = $(".submit");

  Stripe.setPublishableKey(stripe.key);

  // Setup jquery validation defaults
  $.validator.setDefaults({
    highlight: function(element) {
      $(element).addClass("is-danger");
    },
    unhighlight: function(element) {
      $(element).removeClass("is-danger");
    },
    errorElement: 'span',
    errorClass: 'help is-danger'
  });

  // Add routing number validation rule to jquery-validation
  $.validator.addMethod("routingNumber", function(value, element) {
    return Stripe.bankAccount.validateRoutingNumber(value, $("#country").val());;
  }, "That is not a valid routing number.");

  // Add account number validation rule to jQuery-validation
  $.validator.addMethod("accountNumber", function(value, element) {
    return Stripe.bankAccount.validateAccountNumber(value, $("#country").val());;
  }, "That is not a valid account number.");

  // Install above validation rules on form elements
  $form.validate({
    rules: {
      "routing-number" : {
        routingNumber: true,
        required: true
      },
      "account-number" : {
        accountNumber: true,
        required: true
      }
    }
  });

  // intercept form submission after validation, initiate stripe request
  $form.on('submit', function(e) {
    // only halt submission if there's no stripe token
    if (!$("#stripe-token").val()) {
      e.preventDefault();

      // disable submit button
      $submit.prop('disabled', true).addClass("is-loading");

      // initiate stripe request
      Stripe.bankAccount.createToken({
        country: $('#country').val(),
        currency: $('#currency').val(),
        routing_number: $('#routing-number').val(),
        account_number: $('#account-number').val(),
        account_holder_name: $('#account-holder-name').val(),
        account_holder_type: 'individual'
      }, stripeResponseHandler);
    }
  });

  function stripeResponseHandler(status, response) {
    if (response.error) {
      alert(response.error.message); // show error message
      $submit.prop('disabled', false).removeClass("is-loading"); // re-enable submit button
    } else {
      var token = response.id;

      // insert token into form and submit to server
      $("#stripe-token")
        .val(token)
        .closest("form")
        .submit();
    }
  }
});
