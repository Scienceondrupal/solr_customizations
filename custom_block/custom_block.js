(function ($) {


    $(document).ready(function () {
        $('.datepicker').datepicker({
            showOn: 'both',
            buttonImageOnly: true,
            dateFormat: 'mm-dd-yy'
        });



        var temporal_ApplyButton = jQuery("#block-custom-block-temporal").find("input[type='submit']");//jQuery(".form-item-enddate").next();

        temporal_ApplyButton.click(function (e) {
            e.preventDefault();
            var que_or_amp="&";

            if(window.location.href.indexOf("?")<0){
                  que_or_amp="?";
            }
            var start_date_array= jQuery("#edit-startdate").val().split("-");
            var end_date_array= jQuery("#edit-enddate").val().split("-");
            var start_date = start_date_array[2]+'-'+start_date_array[0]+'-'+start_date_array[1]+'T00:00:00Z';
            var end_date = end_date_array[2]+'-'+end_date_array[0]+'-'+end_date_array[1]+'T00:00:00Z';
            var neg_infinity = '2000-01-01T23:59:59.999Z';
            var infinity = '2014-04-29T23:59:59.999Z';
            
            window.location.href=window.location.href+que_or_amp+"f[10]=dm_field_dataset_stop_date:["+start_date+"+TO+"+infinity+"]&f[11]=dm_field_dataset_start_date:["+neg_infinity+"+TO+"+end_date+"]";
        });


        var spatial_ApplyButton = jQuery("#block-custom-block-spatial").find("input[type='submit']");//jQuery("#edit-submit--3");
        spatial_ApplyButton.click(function (e){
            e.preventDefault();
            var que_or_amp="&";

            if(window.location.href.indexOf("?")<0){
                que_or_amp="?";
            }
            var north = jQuery("#edit-north").val();
            var south = jQuery("#edit-south").val();
            var east = jQuery("#edit-east").val();
            var west = jQuery("#edit-west").val();
            window.location.href=window.location.href+que_or_amp+"f[20]=sm_field_spatial:["+west+","+south+"+TO+"+east+","+north+"]";
         
        });


























    });




    // $('.datepicker').datepicker( 'option', 'minDate', 'd' );
})(jQuery);