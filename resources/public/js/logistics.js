$(document).ready(function() {

  // install jquery-validation on the form
  $("form").validate();

  // Show/hide the pet select field depending on the radio (yes/no) choice
  $("#pets-panel input:radio").on("change", function(evt) {
    var result = $(evt.currentTarget).val();
    if (result === "yes") {
      $("#pet-forms").fadeIn();
      $("#pet-forms select").attr("required", true);
    } else {
      $("#pet-forms").fadeOut();
      $("#pet-forms select").attr("required", false);
    }
  });

  // Show/hide dog-specific fields if "dog" is selected in the pet select
  $("#pet-forms select").on("change", function(evt) {
    var selection = $(evt.currentTarget).val();
    if (selection === "dog") {
      $("#pet-forms .dog-field").fadeIn();
      $("#pet-forms .dog-field input").attr("required", true);
    } else {
      $("#pet-forms .dog-field").fadeOut();
      $("#pet-forms .dog-field input").attr("required", false);
    }
  });

});
