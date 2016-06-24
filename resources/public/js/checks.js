$(document).ready(function() {

  // install jquery-validation on the form
  $("form").validate();

  // install FieldKit for formatting of SSN
  var ssnField = new FieldKit.TextField(
    document.getElementById("ssn"),
    new FieldKit.SocialSecurityNumberFormatter()
  );
});
