$(document).ready(function() {
  // Set bootstrap-specific settings for jquery-validation
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
});
