<@markup id="custom-favicons" action="replace" target="favicons" scope="global">
   <!-- Icons -->
   <link rel="shortcut icon" href="${url.context}/res/openesdh/images/openesdh_ico.bmp" type="image/x-icon" />
   <link rel="icon" href="${url.context}/res/openesdh/images/openesdh_ico.bmp" type="image/x-icon" />
</@markup>

<@markup id="custom-login-resources" action="after" target="resources">
 
   <link rel="stylesheet" type="text/css"
    href="${url.context}/res/openesdh/global/components/head/resources.css" >
   </link>
 
</@markup>

<@markup id="roboto-font" action="before" target="resources">
	<link href='http://fonts.googleapis.com/css?family=Roboto:400,300,500,700,100,100italic,300italic,400italic,500italic,700italic,900,900italic&subset=latin,latin-ext' rel='stylesheet' type='text/css'>
</@markup>