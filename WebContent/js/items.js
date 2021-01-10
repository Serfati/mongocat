$(document).ready(function () {

	var idtoremove = 2;

	$("#loading-div-background").css({ opacity: 0.8 });
	$("#loading-div-background2").css({ opacity: 0.8 });
	$("#loading-div2").css({ opacity: 0.8 });

	

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

	function loadPr(){
		showProgressAnimation();
		var mandator = new Object();

		var url = $('#regForm').attr('action');
		$.ajax({
			type: "GET",
			dataType: "json",
			contentType: "application/x-www-form-urlencoded; charset=UTF-8",
			url: url,
			success: function (json) {
				hideProgressAnimation();
				$("#loading-div-background2").show();
				$("#loading-div2").show();
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
		var itemsUrl = $('#url').val()+'';
		
		var url = $('#logForm').attr('action');
		$.ajax({
			type: "GET",
			dataType: "json",
			contentType: "application/x-www-form-urlencoded; charset=UTF-8",
			data: {
				url: itemsUrl
			},
			url: url,
			success: function (json) {
				hideProgressAnimation();
				$("#loading-div-background2").show();
				$("#loading-div2").show();
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


	function getNItems(){
		showProgressAnimation();
		var topn = $('#topn').val()+'';
		if(topn == 'undefined'){
			topn = 0;
		}
		$.ajax({
			type: "GET",
			dataType: "json",
			contentType: "application/x-www-form-urlencoded; charset=UTF-8",
			data: {
				topn: topn
			},
			url: 'items/get_topn_items',
			success: function (json) {
				hideProgressAnimation();

				$('#scoret').html('');
				for(var i = 0; i<json.length;i++){
					var item = json[i];
					var classTr = '';
					if(i % 2 == 0){
						classTr = 'class="alt"';
					}
					$('#scoret').append(
							'<tr '+classTr+'>'+
							'<td>'+(i+1)+'</td>'+
							'<td>'+ item.title+'</td>'+
							'<td>'+item.prodYear+'</td>'+
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
	
	$('#topnc').click(function () {
		getNItems();
	});
	

	$('#regForm').submit(function () {
		loadPr();
		return false;
	});

	$('#logForm').submit(function () {
		loginPr();
		return false;
	});
	


});