<@markup id="beforeheader" action="replace" target="header">
	<div class="company-title">${msg("label.title")}</div>
</@>

<@markup id="removefooter" action="remove" target="footer"></@>

<@markup id="replace-form" action="replace" target="form">
 <#assign el=args.htmlid?html>

 	<div class="login-container">
	 	
	     <form id="${el}-form" accept-charset="UTF-8" method="post" action="${loginUrl}" class="form-fields ${edition}">
	            <@markup id="fields">
	            <input type="hidden" id="${el}-success" name="success" value="${successUrl?replace("@","%40")?html}"/>
	            <input type="hidden" name="failure" value="${failureUrl?replace("@","%40")?html}"/>
	            <div class="form-group hide-label js-hide-label">
	               <label for="${el}-username">${msg("label.username")}</label><br/>
	               <input type="text" id="${el}-username" name="username" maxlength="255" value="<#if lastUsername??>${lastUsername?html}</#if>" placeholder="${msg("label.username")}"/>
	            </div>
	            <div class="form-group hide-label js-hide-label">
	               <label for="${el}-password">${msg("label.password")}</label><br/>
	               <input type="password" id="${el}-password" name="password" maxlength="255" placeholder="${msg("label.password")}"/>
	            </div>

	            <div class="form-group checkbox">
				     <div class="squaredThree">
				      <input type="checkbox" value="None" id="squaredThree" name="check"/>
				      <label for="squaredThree"></label>
				    </div>
				   <span>Sign me in automatically</span>				   
				 </div>
	            </@markup>
	            <@markup id="buttons">
	            <div class="form-group text-center">
	               <input type="submit" id="${el}-submit" class="login-button btn-submit" value="${msg("button.login")}"/>
	           	</div>
	            </@markup>
	         </form>   
	</div>  

	<script type="text/javascript">//<![CDATA[
	   (function(){
	   		function classReg( className ) {
			  return new RegExp("(^|\\s+)" + className + "(\\s+|$)");
			}

			// classList support for class management
			// altho to be fair, the api sucks because it won't accept multiple classes at once
			var hasClass, addClass, removeClass;

			if ( 'classList' in document.documentElement ) {
			  hasClass = function( elem, c ) {
			    return elem.classList.contains( c );
			  };
			  addClass = function( elem, c ) {
			    elem.classList.add( c );
			  };
			  removeClass = function( elem, c ) {
			    elem.classList.remove( c );
			  };
			}
			else {
			  hasClass = function( elem, c ) {
			    return classReg( c ).test( elem.className );
			  };
			  addClass = function( elem, c ) {
			    if ( !hasClass( elem, c ) ) {
			      elem.className = elem.className + ' ' + c;
			    }
			  };
			  removeClass = function( elem, c ) {
			    elem.className = elem.className.replace( classReg( c ), ' ' );
			  };
			}

			function toggleClass( elem, c ) {
			  var fn = hasClass( elem, c ) ? removeClass : addClass;
			  fn( elem, c );
			}


	   		var labelContainers = document.getElementsByClassName('js-hide-label');

	   		for (var i = 0, len = labelContainers.length; i < len; i++ ) { 
	   			var input = labelContainers[i].querySelector('input'),
	   				placeholderValue = input.getAttribute('placeholder');

	   			(function(i, p){
	   				input.addEventListener('focus', function(){	
		   				removeClass(labelContainers[i], 'hide-label');
		   				this.setAttribute('placeholder', '');

		   			}, false);
	   				input.addEventListener('blur', function(){	
		   				addClass(labelContainers[i], 'hide-label');
		   				this.setAttribute('placeholder', p);
		   			}, false);

	   			})(i, placeholderValue);
	   		}
	   	
	   })();
	 //]]></script>
</@>