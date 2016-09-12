var ach = {};

ach.verify = function() {
  var $form = $("form");

  // Set up Stripe w/ api key
  Stripe.setPublishableKey(stripe.key);

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
      $form.find('button').prop('disabled', true);

      // initiate stripe request
      Stripe.bankAccount.createToken({
        country: $('#country').val(),
        currency: $('#currency').val(),
        routing_number: $('#routing-number').val(),
        account_number: $('#account-number').val(),
        account_holder_name: $('#account-holder-name').val(),
        account_holder_type: $('#account-holder-type').val()
      }, stripeResponseHandler);
    }
  });

  function stripeResponseHandler(status, response) {
    if (response.error) {
      alert(response.error.message); // show error message
      $form.find('button').prop('disabled', false); // re-enable submit button
    } else {
      var token = response.id;

      // hide any visible error messages
      $form.find('.alert-error').fadeOut();

      // insert token into form and submit to server
      $("#stripe-token")
        .val(token)
        .closest("form")
        .submit();
    }
  }

  // The microdeposits form uses Materialize selects that need to be initialized
  installMaterialSelects();

  // TODO: This doesn't display, because the .alert-error container is rendered
  // by server iff there are server-side errors.
  function alert(message) {
    $form.find('.alert-error')
      .fadeIn()
      .find('.alert-text')
      .text(message);
  }
};



ach.pay = function() {
  // The elements we'll be interacting with.
  var $payBtn = $("#pay-btn");
  var $modal = $("#confirmation-modal");
  var $submitBtn = $("#submit");
  var $form = $("form");

  // When an payment choice is made, enable the pay button.
  $("input:radio").on("change", function() {
    $payBtn.prop("disabled", false).removeClass("disabled");
  });

  // When the pay button is pressed, show the confirmation modal
  $payBtn.click(function() {
    $modal.openModal();
  });

  // When the submit button is pressed, submit the form!
  $submitBtn.click(function() {
    $form.submit();
  });
};



$(document).ready(function() {
  // ad hoc module system

  function formValidationOnly() {
    $("form").validate();
  }

  var paths = {
    '/application/logistics': logistics,
    '/application/personal': personal,
    '/application/community': community,
    '/application/submit': submit,
    '/onboarding/security-deposit/payment-method': formValidationOnly,
    '/onboarding/security-deposit/payment-method/ach/verify': ach.verify,
    '/onboarding/security-deposit/payment-method/ach/pay': ach.pay,
    '/account': function() {
      $("form").validate();
    }
  };

  // activate side menu
  $(".button-collapse").sideNav();

  setupFormValidation();

  var jsForURI = paths[window.location.pathname];
  if (jsForURI) {
    if (Array.isArray(jsForURI)) {
      jsForURI.forEach(function(f) { f(); });
    } else {
      jsForURI();
    }
  } else {
    console.warn("No JS registered for: " + window.location.pathname);
  }
});

function logistics() {
  installMaterialSelects();

  // install jquery-validation on the form
  $("form").validate();

  // Show/hide the pet select field depending on the radio (yes/no) choice
  $("#pets-section input:radio").on("change", function(evt) {
    var result = $(evt.currentTarget).val();
    if (result === "yes") {
      $("#pet-inputs").fadeIn();
      $("#pet-inputs select").attr("required", true);
    } else {
      $("#pet-inputs").fadeOut();
      $("#pet-inputs select").attr("required", false);
    }
  });

  // Show/hide dog-specific fields if "dog" is selected in the pet select
  $("#pet-inputs select").on("change", function(evt) {
    var selection = $(evt.currentTarget).val();
    if (selection === "dog") {
      $("#pet-inputs .dog-field").fadeIn();
      $("#pet-inputs .dog-field input").attr("required", true);
    } else {
      $("#pet-inputs .dog-field").fadeOut();
      $("#pet-inputs .dog-field input").attr("required", false);
    }
  });

  $("#license-section input:radio").on("change", function(evt) {
    var licenseSelection = $(evt.currentTarget).val();

    $("#properties-section input:checkbox")
      .each(function(_, input) {
        var propertyId = $(input).attr("id");
        $(input)
          .siblings("label")
          .children(".license-price")
          .fadeIn()
          .text(formatPrice(getPrice(propertyId)));
      });

    function formatPrice(price) {
      if (typeof price === "string") {
        return price;
      }
      return "$" + price + "/mo";
    }

    function getPrice(forProperty) {
      for (var propertyId in licenseMapping) {
        var property = licenseMapping[propertyId];
        var price = property[licenseSelection];
        if (propertyId === forProperty) {
          return price;
        }
      }
      return "";
    }
  });
}

function personal() {
  // Install field-kit on phone number
  var fk = new FieldKit.TextField(document.getElementById("phone"),
                                  new FieldKit.PhoneFormatter());

  // install jquery-validation on the form
  $("form").validate();
  // setup materialize components
  installMaterialSelects();
  initializeDatePicker();

  function initializeDatePicker() {
    var today = new Date();

    $(".datepicker").pickadate({
      selectYears: 40,
      selectMonths: true,
      max: new Date(today.getFullYear() - 18, today.getMonth(), today.getDate()),
      today: '',
      formatSubmit: 'yyyy-mm-dd',
      hiddenName: true
    });
  }
}

function community() {
  $('form').validate();
}

function submit() {
  var receiveCopyCheckbox = $('#receive-copy');
  var paymentSent = false;
  var linkButton = $("#link-button");
  var linkButtonEnabled = plaid.complete === false;
  var alertTypes = {
    success: "alert-success",
    error: "alert-danger"
  };

  // Stripe Configuration
  var handler = StripeCheckout.configure({
    name: "Starcity",
    description: "Member Application",
    amount: stripe.amount,
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
        .closest("form")
        .submit();
    }
  });

  // Intercept submits to collect payment
  $("form").validate({
    submitHandler: function(form) {
      if (!paymentSent) {
        handler.open();
      } else {
        form.submit();
        $("#submit-section button").fadeOut();
        $("#submit-section #loading").fadeIn();
      }
    }
  });

  // Close Stripe on nav
  $(window).on('popstate', function() {
    handler.close();
  });

  // Display modal with compliance info when the `[ ] Yes` box is unchecked
  $("#background-permission").click(function(evt) {
    var checked = !evt.currentTarget.checked;
    if (!checked) {
      evt.preventDefault();
      $("#background-check-modal").openModal();
    } else {
      receiveCopyCheckbox.fadeOut();
    }
  });

  // Check box when agree button pushed on modal
  $("#background-check-agree").click(function(evt) {
    evt.preventDefault();
    $("#background-permission").prop("checked", true);
    receiveCopyCheckbox.fadeIn();
  });


  // NOTE: `plaid` variable is populated by server as a var holding some JSON.
  var linkHandler = Plaid.create({
    env: plaid.env,
    clientName: 'Starcity',
    product: 'auth',
    key: plaid.key,
    onSuccess: sendPublicToken
  });

  // Present Plaid Link when button is pushed
  linkButton.click(function() {
    if (linkButtonEnabled) {
      linkHandler.open();
    }
  });

  $("#income-section .modal-trigger").click(function(e) {
    e.preventDefault();
    $("#bank-account-info-modal").openModal();
  });

  // ==============================
  // Plaid Helper Functions

  function sendPublicToken(public_token, metadata) {
    toggleLinkButton();         // disable link button while sending to server
    $.post("/api/v1/plaid/verify/income", {public_token: public_token})
      .done(function(data) {
        linkButton
          .addClass("disabled")
          .text("Verified")
          .prepend("<i class='material-icons right'>done</i>");
        linkButtonEnabled = false;
      })
      .fail(function(data) {
        addPlaidAlert("error", "Whoops! Something went wrong &mdash; please try linking your account again.");
        toggleLinkButton();     // re-enable link button to try again
      });
  }

  function addPlaidAlert(type, content) {
    var alertClass = alertTypes[type];
    $("#income-section").prepend("<div class='alert " + alertClass + "' role='alert'>" + content + "</div>");
  }

  function toggleLinkButton() {
    linkButton.toggleClass('disabled');
    linkButtonEnabled = !linkButtonEnabled;
  }
}



function installMaterialSelects() {
  // install material design select
  $('select').material_select();

  // hack to allow jquery-validation to work on materialize selects
  $("select[required]").css({display: "inline", height: 0, padding: 0, width: 0, border: "none"});
}

function setupFormValidation() {
  $.validator.setDefaults({
    highlight: function(element) {
      $(element).closest('.validation-group').addClass('has-error');
      $(element).closest('.input-field').addClass('has-error');
      if ($(element).is("select")) {
        $(element)
          .closest(".select-wrapper")
          .find("input.select-dropdown")
          .addClass("invalid");
      } else {
        $(element).addClass('invalid');
      }
    },
    unhighlight: function(element) {
      var inputGroup = $(element).closest('.validation-group');
      inputGroup.removeClass('has-error');
      $(element).closest('.input-field').removeClass('has-error');
      $(element).removeClass('invalid');
      // NOTE: Need this specifically for selects. Shouldn't be any harm in
      // having this here...
      inputGroup.find('.error-block').css({display: 'none'});
    },
    errorElement: 'span',
    errorClass: 'error-block',
    errorPlacement: function(error, element) {
      if(element.parent('.validation-group').length) {
        error.insertAfter(element.parent());
      } else if ($(element).closest('.validation-group').length > 0) {
        $(element).closest('.validation-group').append(error);
      } else {
        error.insertAfter(element);
      }

    }
  });
}
