<?php

//解析参数
function parseArgs() {
    $args = array();
    foreach ($_SERVER["argv"] as $key => $value) {
        if($key > 0) {
            $split = explode("=", $value);
            $args[substr($split[0], 2)] = $split[1];
        }
    }
    return $args;
}
$args = parseArgs();

//填充文件内容
function traverse($path = '.') {
    $fileList = array();
    $current_dir = opendir($path);
    $count = 0;
    while(($file = readdir($current_dir)) !== false) {
        $sub_dir = $path . DIRECTORY_SEPARATOR . $file;
        if($file == '.' || $file == '..' || substr($file, 0, 1) == '.') {
            continue;
        } else if(is_dir($sub_dir)) {
            traverse($sub_dir);
        } else {
            $count ++;
        }
    }
    if($count == 0) {
        touch($path . "/" . "index.html");
    }
}
traverse($args["dir"]);
