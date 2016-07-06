$(document).ready(function() {

  // install jquery-validation on the form
  $("form").validate();

  // TODO: Remove repetition
  // Show/hide the pet select field depending on the radio (yes/no) choice
  $("#pets-panel input:radio").on("change", function(evt) {
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

});
