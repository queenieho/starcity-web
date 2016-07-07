$(document).ready(function() {
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
    product: 'connect',
    key: plaid.key,
    onSuccess: sendPublicToken,
    onExit: function() {
      console.log("Exited!");
    }
  });

  // install jquery-validation on the form
  $("form").validate();

  function sendPublicToken(public_token, metadata) {
    toggleLinkButton();         // disable link button while sending to server
    $.post("/api/v1/plaid/auth", {public_token: public_token})
      .done(function(data) {
        linkButton
          .removeClass("btn-info")
          .addClass("btn-success disabled")
          .text("Thanks!")
          .prepend("<span class='glyphicon glyphicon-ok'>&nbsp;</span>");
        linkButtonEnabled = false;
      })
      .fail(function(data) {
        // TODO: Indicate that there was a problem in UI
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
});
