<?php

//Drupal configuration
$_SERVER['HTTP_HOST'] = 'wwwdev.itsc.uah.edu';
$_SERVER['REMOTE_ADDR'] = '146.229.234.20';
$DRUPAL7_ROOT = "/web/wwwdev.itsc.uah.edu/html/sod";

define('DRUPAL_ROOT', $DRUPAL7_ROOT);
chdir($DRUPAL7_ROOT);
require_once "./includes/bootstrap.inc";
drupal_bootstrap(DRUPAL_BOOTSTRAP_FULL);


//***********************************************************************


$path1 = '/tmp/data/';
$files = scandir($path1);
foreach ($files as $file) {
    $file_parts = pathinfo($file);
    if ($file_parts['extension'] == "csv") {
       // echo $file;
$date = date('Y/m/d H:i:s');

echo $file."\t"."Start time=".$date."\n";

$path=$path1.$file;
$row = 1;
if (($handle = fopen($path, "r")) !== FALSE) {
    while (($data = fgetcsv($handle, 1000, ",")) !== FALSE) {
        if ($row == 1) {
            $header = $data;
            // print_r($header);
        } else { // other row than header
            $node = new stdClass();
            $node->type = 'inventory';
            //echo(count($data));
            for ($j = 0; $j < count($data); $j++) {
                if ($header[$j] == "START_DATE" || $header[$j] == "STOP_DATE" || $header[$j] == "ENTERED")
                    $value = "" . date("Y-m-d H:i:s", strtotime($data[$j])) . ""; //cast to date
                else if ($header[$j] == "BYTE_SIZE")
                    $value = $data[$j];
                else
                    $value = "" . $data[$j] . "";

                //Mapping for individual fields,
                if ($header[$j] == "GRANULE_NAME")
                    $node->title = $value;
                else if ($header[$j] == "GRANULE_COMMENTS")
                    $node->field_granule_comments1[und][0][value] = $value;
                else if ($header[$j] == "DS_SHORT_NAME")
                    $node->field_dataset_short1_name[und][0][value] = $value;
                else if ($header[$j] == "START_ORBIT")
                    $node->field_start_orbit1[und][0][value] = $value;
                else if ($header[$j] == "STOP_ORBIT")
                    $node->field_stop_orbit1[und][0][value] = $value;
                else if ($header[$j] == "START_DATE")
                    $node->field_start_date[und][0][value][date] = $value;
                else if ($header[$j] == "STOP_DATE")
                    $node->field_stop_date[und][0][value][date] = $value;
                else if ($header[$j] == "ENTERED")
                    $node->field_archive_date2[und][0][value][date] = $value;
                else if ($header[$j] == "MISSING")
                    $node->field_missing1[und][0][value] = $value;
                else if ($header[$j] == "PATH")
                    $node->field_path1[und][0][value] = $value;
                else if ($header[$j] == "BROWSE_IMAGE_FILE")
                    $node->field_browse_graphi1_information[und][0][value] = $value;
                else if ($header[$j] == "BYTE_SIZE")
                    $node->field_byte_size[und][0][value] = $value;
                else if ($header[$j] == "NUM_PASSES")
                    $node->field_number_of_passes[und][0][value] = $value;
                else if ($header[$j] == "HOST")
                    $node->field_host[und][0][value] = $value;
                else if ($header[$j] == "ENV")
                    $node->field_env[und][0][value] = $value;
            }

		$node->uid = 3;
        $node->promote = 1;
        $node->status = 1;
        $node->language = 'und';

        node_save($node);
        }

        $row++;
    }
    fclose($handle);
}
}
}
