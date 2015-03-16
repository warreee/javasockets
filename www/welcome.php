<?php
/**
 * Created by PhpStorm.
 * User: warreee
 * Date: 3/4/15
 * Time: 5:07 PM
 */

print_r($_POST);

$myfile = fopen("newfile.txt", "w") or die("Unable to open file!");
$txt = "John Doe\n";
fwrite($myfile, implode(",",$_POST));

fclose($myfile);

?>