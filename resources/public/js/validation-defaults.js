$(document).ready(function() {
  // Set bootstrap-specific settings for jquery-validation
  $.validator.setDefaults({
    highlight: function(element) {
      $(element).closest('.form-group').addClass('has-error');
    },
    unhighlight: function(element) {
      $(element).closest('.form-group').removeClass('has-error');
    },
    errorElement: 'span',
    errorClass: 'help-block',
    errorPlacement: function(error, element) {
      if(element.parent('.input-group').length) {
        error.insertAfter(element.parent());
      } else if ($(element).closest('.form-group').length > 0) {
        $(element).closest('.form-group').append(error);
      } else {
        error.insertAfter(element);
      }
    }
  });
});
