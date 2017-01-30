

// Fetching data from couchdb
$.get({
    url: "http://115.146.88.113:5984/analysis/_design/companies/_view/all",
    dataType: "json",
    success: function( result ) {
        if (result.total_rows > 0) {
            // Populate the combo select box with names of companies.
            var options = $("#company-select");
            $.each(result.rows, function() {
                options.append($("<option />").val(this.key).text(this.value.company));
            });
            localStorage.setItem("companies", JSON.stringify(result.rows));
        } else {
            console.log("Error Fetching Company names.");
            localStorage.setItem("companies", "");
        }
    }
});


$(document).ready(function() {
	
	
	//Run on clicking view change # 123
    $("#companySelectForm1").submit(function(e) {
        var option = $("#company-select").find(":selected");
        var name = option.text();
        var companyId = option.val();
        var companies = JSON.parse(localStorage.getItem("companies"));

        
        var item = companies.find(function(i) {
            return i.id == companyId;
        });

        console.log(item);
        
        // Draw column chart 00 
        var chart = new CanvasJS.Chart("analysis00", {
			title: {
				text: "Before"
			},
			data: [{
				type: "column",
				dataPoints: [
					{ y: item.value.LingposBefore , label: "Positive" },
					{ y: item.value.LingneuBefore, label: "Neutral" },
					{ y: item.value.LingnegBefore, label: "Negative" }
				]
			}]
		});
		chart.render();

        // Draw column chart 01
        var chart = new CanvasJS.Chart("analysis01", {
			title: {
				text: "After"
			},
			data: [{
				type: "column",
				dataPoints: [
					{ y: item.value.LingposAfter , label: "Positive" },
					{ y: item.value.LingneuAfter, label: "Neutral" },
					{ y: item.value.LingnegAfter, label: "Negative" }
				]
			}]
		});
		chart.render();

        // Draw column chart 10
        var chart = new CanvasJS.Chart("analysis10", {
			title: {
				text: "Before"
			},
			data: [{
				type: "column",
				dataPoints: [
					{ y: item.value.NLTKposBefore , label: "Positive" },
					{ y: item.value.NLTKnegBefore, label: "Negative" }
				]
			}]
		});
		chart.render();

        // Draw column chart 11
        var chart = new CanvasJS.Chart("analysis11", {
			title: {
				text: "After"
			},
			data: [{
				type: "column",
				dataPoints: [
					{ y: item.value.NLTKposAfter , label: "Positive" },
					{ y: item.value.NLTKnegAfter, label: "Negative" }
				]
			}]
		});
		chart.render();
        

        // Draw column chart 20
        var chart = new CanvasJS.Chart("analysis20", {
			title: {
				text: "Before"
			},
			data: [{
				type: "column",
				dataPoints: [
					{ y: item.value.TextblobBefore , label: "Polarity" }
				]
			}]
		});
		chart.render();


        // Draw column chart 21
        var chart = new CanvasJS.Chart("analysis21", {
			title: {
				text: "After"
			},
			data: [{
				type: "column",
				dataPoints: [
					{ y: item.value.TextblobAfter , label: "Polarity" }
				]
			}]
		});
		chart.render();

        return false;
    });
	// Sentiment analysis*******************************************************************************************************
	$("#companySelectForm2").submit(function(e) {
        var option = $("#company-select").find(":selected");
        var name = option.text();
        var companyId = option.val();
        var companies = JSON.parse(localStorage.getItem("companies"));

        
        var item = companies.find(function(i) {
            return i.id == companyId;
        });

        console.log(item);
        
        // Draw column chart 00 
        var chart = new CanvasJS.Chart("analysisling", {
			title: {
				text: "Overall"
			},
			data: [{
				type: "column",
				dataPoints: [
					{ y: item.value.Lingpos , label: "Positive" },
					{ y: item.value.Lingneu, label: "Neutral" },
					{ y: item.value.Lingneg, label: "Negative" }
				]
			}]
		});
		chart.render();

        

        // Draw column chart 10
        var chart = new CanvasJS.Chart("analysisnltk", {
			title: {
				text: "Overall"
			},
			data: [{
				type: "column",
				dataPoints: [
					{ y: item.value.NLTKpos , label: "Positive" },
					{ y: item.value.NLTKneg, label: "Negative" }
				]
			}]
		});
		chart.render();

        
        

        // Draw column chart 20
        var chart = new CanvasJS.Chart("analysistextblob", {
			title: {
				text: "Overall"
			},
			data: [{
				type: "column",
				dataPoints: [
					{ y: item.value.Textblob , label: "Polarity" }
				]
			}]
		});
		chart.render();


        

        return false;
    });
	
	// Followers ***************************************************************************************************
	$("#companySelectForm3").submit(function(e) {
        var option = $("#company-select").find(":selected");
        var name = option.text();
        var companyId = option.val();
        var companies = JSON.parse(localStorage.getItem("companies"));

        
        var item = companies.find(function(i) {
            return i.id == companyId;
        });

        console.log(item);
        
        // Draw column chart 00 
        var chart = new CanvasJS.Chart("analysisfollowers", {
			title: {
				text: "Percentage of tweets from the host country of company"
			},
			data: [{
				type: "column",
				dataPoints: [
					{ y: item.value.national, label: "National" }
				]
			}]
		});
		chart.render();

        
        return false;
    });
	// add more cases before this point
});
