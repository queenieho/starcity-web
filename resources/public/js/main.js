$(document).ready(function() {
  // ad hoc module system
  var paths = {
    '/application/logistics': logistics,
    '/application/personal': personal,
    '/application/community': community,
    '/application/submit': submit
  };

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
}

function personal() {
  var linkButton = $("#link-button");
  var linkButtonEnabled = plaid.complete === false;
  var alertTypes = {
    success: "alert-success",
    error: "alert-danger"
  };

  // NOTE: `plaid` variable is populated by server as a var holding some JSON.
  var linkHandler = Plaid.create({
    env: plaid.env,
    clientName: 'Starcity',
    product: 'auth',
    key: plaid.key,
    onSuccess: sendPublicToken,
    onExit: function() {
      console.log("Exited!");
    }
  });

  // install jquery-validation on the form
  $("form").validate();
  // setup materialize components
  installMaterialSelects();
  initializeDatePicker();

  function sendPublicToken(public_token, metadata) {
    toggleLinkButton();         // disable link button while sending to server
    $.post("/api/v1/plaid/auth", {public_token: public_token})
      .done(function(data) {
        linkButton
          .addClass("disabled")
          .text("Account Linked!") // TODO:
          .prepend("<i class='material-icons right'>done</i.");
        linkButtonEnabled = false;
      })
      .fail(function(data) {
        addPlaidAlert("error", "Whoops! Something went wrong &mdash; please try linking your account again.");
        toggleLinkButton();     // re-enable link button to try again
      });
  }

  function addPlaidAlert(type, content) {
    var alertClass = alertTypes[type];
    $("#plaid-section").prepend("<div class='alert " + alertClass + "' role='alert'>" + content + "</div>");
  }

  function toggleLinkButton() {
    linkButton.toggleClass('disabled');
    linkButtonEnabled = !linkButtonEnabled;
  }

  linkButton.click(function() {
    if (linkButtonEnabled) {
      linkHandler.open();
    }
  });

  function initializeDatePicker() {
    var today = new Date();

    $(".datepicker").pickadate({
      selectYears: true,
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
        .closest("form")
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
}

function installMaterialSelects() {
  // install material design select
  $('select').material_select();

  // hack to allow jquery-validation to work on materialize selects
  $("select[required]").css({display: "inline", height: 0, padding: 0, width: 0, border: "none"});
}
