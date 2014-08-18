<?php

$this_file = basename(__FILE__);

$this_dir = dirname(__FILE__);

$target_dir = realpath($this_dir . '/../../../') . '/target/';

/**
 * This PHP page can be invoked with a "filename" HTTP parameter:
 * In that case, it serves the corresponding PNG file
 * from the local "target/" directory.
 */
if (isset($_GET['filename'])) {

	$filename = $_GET['filename'];

	echo file_get_contents($target_dir . $filename);

	die;
}

header('Content-Type: text/html; charset=UTF-8');

?><!DOCTYPE html>
<html lang="en" dir="ltr">
<head>
<title>generateGoogleMapsMarkers</title>
</head>
<body>
	<h1>generateGoogleMapsMarkers</h1>

<div style="border:1px solid #900; padding:8px; background-color:#ffc; display:table;">
	This PHP page loads and displays 1x- and 2x- 
	generated icons for our custom markers to use in Google Maps.
	<br>
	Save the generated images into the local “target/” directory,
	then reload the PHP page to check them.
</div>

<p/>

<?php

function remote_img($filename, $url) {

	$id = @ereg_replace('[\@\.]', '_', $filename);

	echo '
	<img id="' . $id . '"
		alt="' . $filename . '"
		width="22" 
		title="' . $filename . '" 
		src="' . $url . '">';
}

function local_img($filename, $url) {

	global $this_file;

	$id = @ereg_replace('[\@\.]', '_', $filename);

	// To display the image file saved locally in the "target/" directory, 
	// invoke this PHP page with a "filename" HTTP parameter:

	echo '
	<img id="local_' . $id . '"
		alt="' . $filename . '"
		width="22" 
		title="' . $filename . '" 
		src="' . $this_file . '?filename=' . $filename . '">';
}

function imgs($img) {

	for ($c = 'A';; ++$c) {

		$img('marker_green' . $c . '.png', // 22x40
			'http://mt.google.com/vt/icon?psize=15&font=fonts/Roboto-Regular.ttf'
				. '&color=ff330000&name=icons/spotlight/spotlight-waypoint-a.png'
				. '&ax=44&ay=47'
				. '&scale=1'
				. '&text=' . $c
		);

		if ($c == 'Z') break;
	}

	$img('marker_greenEllipsis.png', // 22x40
		'http://mt.google.com/vt/icon?psize=15&font=fonts/Roboto-Regular.ttf'
			. '&color=ff330000&name=icons/spotlight/spotlight-waypoint-a.png'
			. '&ax=44&ay=39'
			. '&scale=1'
			. '&text=…'
	);

	echo '
	<br>
	';

	for ($c = 'A';; ++$c) {
		
		$img('marker_green'. $c . '@2x.png', // 44x80
			'http://mt.google.com/vt/icon?psize=15&font=fonts/Roboto-Regular.ttf'
				. '&color=ff330000&name=icons/spotlight/spotlight-waypoint-a.png'
				. '&ax=44&ay=47'
				. '&scale=2'
				. '&text=' . $c
		);

		if ($c == 'Z') break;
	}

	$img('marker_greenEllipsis@2x.png', // 44x80
		'http://mt.google.com/vt/icon?psize=15&font=fonts/Roboto-Regular.ttf'
			. '&color=ff330000&name=icons/spotlight/spotlight-waypoint-a.png'
			. '&ax=44&ay=38'
			. '&scale=2'
			. '&text=…'
	);

	echo '
	<p>
	';
}

echo 'Generation by http://mt.google.com/: Remote images:<br>';

imgs('remote_img');

echo 'Comparison with files saved locally in the “target/” directory:<br>';

imgs('local_img');

?>

</body>
</html>
