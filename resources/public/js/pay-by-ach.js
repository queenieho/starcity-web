$(document).ready(function() {
  // The elements we'll be interacting with.
  var $payBtn = $("#pay-btn");
  var $modal = $("#confirmation-modal");
  var $submitBtn = $("#submit");
  var $form = $("form");

  // When an payment choice is made, enable the pay button.
  $("input:radio").on("change", function() {
    $payBtn.prop("disabled", false).removeClass("is-disabled");
  });

  // When the pay button is pressed, show the confirmation modal
  $payBtn.click(function() {
    $modal.addClass("is-active");
  });

  // When the submit button is pressed, submit the form!
  $submitBtn.click(function() {
    $form.submit();
  });

  $(".modal-background").click(closeModal);
  $(".modal-close").click(closeModal);

  function closeModal() {
    $modal.removeClass("is-active");
  }
});
