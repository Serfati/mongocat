$(document).ready(function () {

	var idtoremove = 2;

	$("#loading-div-background").css({ opacity: 0.8 });
	$("#loading-div-background2").css({ opacity: 0.8 });
	$("#loading-div2").css({ opacity: 0.8 });

	var sPageURL = window.location.search.substring(1);
	if(sPageURL != null &&  sPageURL.indexOf('confirm') == 0){
		$("#loading-div-background2").show();
		$("#loading-div2").show();
	}

	function showProgressAnimation() {
		$("#loading-div-background").show();
	}


	function hideProgressAnimation() {
		$("#loading-div-background").hide();
	}





	$('#closegrey').click(function () {
		$("#loading-div-background2").hide();
		$("#loading-div2").hide();
	});


	function setMessagePopUp(type, text,isreg){
		hideProgressAnimation();
		if(type == 'fine'){
			if(isreg){
				$('#regContainer').html('<p  style="color:green;">'+text+'</p>');
			}else{
				$('#logContainer').html('<p  style="color:green;">'+text+'</p>');
			}

		}else{
			$('p.error').remove();
			if(isreg){
				$('#regadd').before('<p class="error" style="color:red;">'+text+'</p>');
			}else{
				$('#loginadd').before('<p class="error" style="color:red;">'+text+'</p>');
			}

		}
	}

	function registrationPr(){
		showProgressAnimation();
		var mandator = new Object();
		mandator.username = $('#username').val()+'';
		mandator.password = $('#password').val()+'';
		mandator.firstName = $('#fname').val()+'';
		mandator.lastName = $('#lname').val()+'';

		var url = $('#regForm').attr('action');
		$.ajax({
			type: "POST",
			dataType: "json",
			contentType: "application/x-www-form-urlencoded; charset=UTF-8",
			data: {
				username: mandator.username,
				password: mandator.password,
				firstName: mandator.firstName,
				lastName: mandator.lastName
			},
			url: url,
			statusCode: {
				409: function (jqXHR, textStatus, errorThrown) {
					setMessagePopUp("problem", 'Team Name "'+mandator.username+'" already exist, please use a new one',true);
				},
				408: function (jqXHR, textStatus, errorThrown) {
					setMessagePopUp("problem", 'Email "'+mandator.username+'" already exist, please use a new one',true);
				}
			},
			success: function (json) {
				setMessagePopUp("fine","Thank you for the registration!",true);
			},
			error: function (jqXHR, textStatus, errorThrown) {
				if(jqXHR.status != null && jqXHR.status == 403)
				{
					setMessagePopUp("problem", "You do not have sufficient privileges to perform this action.",true);
				}
				else if(jqXHR.status != null && jqXHR.status == 401)
				{
					setMessagePopUp("problem", "Your session is expired. Please login.",true);
				}
				else if(jqXHR.status != null && jqXHR.status == 400)
				{
					setMessagePopUp("problem", "Error: Please check your entries.",true);
				}
				else if(jqXHR.status != null && jqXHR.status == 404)
				{
					setMessagePopUp("problem", "Registration url is not available.",true);
				}
				else if(jqXHR.status != null && jqXHR.status == 500)
				{
					setMessagePopUp("problem", "The server does not answer correctly. Please try again later.",true);
				}
				else
				{
					setMessagePopUp("problem", "The server does not answer correctly. Please try again later.",true);
				}

			}
		}); 
	}

	function loginPr(){
		showProgressAnimation();
		var mandator = new Object();
		mandator.username = $('#usernamel').val()+'';
		mandator.password = $('#passwordl').val()+'';

		var url = $('#logForm').attr('action');
		$.ajax({
			type: "POST",
			dataType: "json",
			contentType: "application/x-www-form-urlencoded; charset=UTF-8",
			data: {
				username: mandator.username,
				password: mandator.password
			},
			url: url,
			success: function (json) {

				if(json){
					setMessagePopUp("fine","Welcome to the system!",false);
				}else{
					setMessagePopUp("problem", "Incorrect username or password.",false);
				}
			},
			error: function (jqXHR, textStatus, errorThrown) {
				if(jqXHR.status != null && jqXHR.status == 403)
				{
					setMessagePopUp("problem", "You do not have sufficient privileges to perform this action.",false);
				}
				else if(jqXHR.status != null && jqXHR.status == 401)
				{
					setMessagePopUp("problem", "Your session is expired. Please login.",false);
				}
				else if(jqXHR.status != null && jqXHR.status == 400)
				{
					setMessagePopUp("problem", "Error: Please check your entries.",false);
				}
				else if(jqXHR.status != null && jqXHR.status == 404)
				{
					setMessagePopUp("problem", "Registration url is not available.",false);
				}
				else if(jqXHR.status != null && jqXHR.status == 500)
				{
					setMessagePopUp("problem", "The server does not answer correctly. Please try again later.",false);
				}
				else
				{
					setMessagePopUp("problem", "The server does not answer correctly. Please try again later.",false);
				}

			}
		}); 
	}

	function showAmount(){
		showProgressAnimation();
		$.ajax({
			type: "GET",
			dataType: "json",
			contentType: "application/x-www-form-urlencoded; charset=UTF-8",
			data: {
				days: 5,
			},
			url: 'registration/get_number_of_registred_users',
			success: function (json) {
				hideProgressAnimation();
				$('#num_reg_users').html('Number of registred users in the last 5 days: '+json);
			},
			error: function (jqXHR, textStatus, errorThrown) {
				if(jqXHR.status != null && jqXHR.status == 403)
				{
					setMessagePopUp("problem", "You do not have sufficient privileges to perform this action.",false);
				}
				else if(jqXHR.status != null && jqXHR.status == 401)
				{
					setMessagePopUp("problem", "Your session is expired. Please login.",false);
				}
				else if(jqXHR.status != null && jqXHR.status == 400)
				{
					setMessagePopUp("problem", "Error: Please check your entries.",false);
				}
				else if(jqXHR.status != null && jqXHR.status == 404)
				{
					setMessagePopUp("problem", "Registration url is not available.",false);
				}
				else if(jqXHR.status != null && jqXHR.status == 500)
				{
					setMessagePopUp("problem", "The server does not answer correctly. Please try again later.",false);
				}
				else
				{
					setMessagePopUp("problem", "The server does not answer correctly. Please try again later.",false);
				}

			}
		}); 
	}

	function getAllUsers(){
		showProgressAnimation();
		var url = $('#logForm').attr('action');
		$.ajax({
			type: "GET",
			dataType: "json",
			contentType: "application/x-www-form-urlencoded; charset=UTF-8",
			url: 'registration/get_all_users',
			success: function (json) {
				hideProgressAnimation();

				
				for(var i = 0; i<json.length;i++){
					var user = json[i];
					var classTr = '';
					if(i % 2 == 0){
						classTr = 'class="alt"';
					}
					$('#scoret').append(
							'<tr '+classTr+'>'+
							'<td>'+(i+1)+'</td>'+
							'<td>'+ user.username+'</td>'+
							'<td>'+user.firstName+'</td>'+
							'<td>'+user.lastName+'</td>'+
					'</tr>');

				}

			},
			error: function (jqXHR, textStatus, errorThrown) {
				if(jqXHR.status != null && jqXHR.status == 403)
				{
					setMessagePopUp("problem", "You do not have sufficient privileges to perform this action.",false);
				}
				else if(jqXHR.status != null && jqXHR.status == 401)
				{
					setMessagePopUp("problem", "Your session is expired. Please login.",false);
				}
				else if(jqXHR.status != null && jqXHR.status == 400)
				{
					setMessagePopUp("problem", "Error: Please check your entries.",false);
				}
				else if(jqXHR.status != null && jqXHR.status == 404)
				{
					setMessagePopUp("problem", "Registration url is not available.",false);
				}
				else if(jqXHR.status != null && jqXHR.status == 500)
				{
					setMessagePopUp("problem", "The server does not answer correctly. Please try again later.",false);
				}
				else
				{
					setMessagePopUp("problem", "The server does not answer correctly. Please try again later.",false);
				}

			}
		}); 
	}
	
	getAllUsers();

	$('#regForm').submit(function () {
		registrationPr();
		return false;
	});

	$('#logForm').submit(function () {
		loginPr();
		return false;
	});
	showAmount();


});