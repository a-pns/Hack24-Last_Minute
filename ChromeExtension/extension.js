//Place script that manipulates document here
var newData;
var entityClicked = false;
var contextParseWidgetClicked = false;

var div=document.createElement("div");
document.body.insertBefore(div, document.body.firstChild);
div.innerText='Contextualise';
div.id = 'contextParseWidget';

var widgetBox=document.createElement("div");
document.body.insertBefore(widgetBox, document.body.firstChild);
widgetBox.id = 'widgetBox';

document.getElementById('contextParseWidget').addEventListener('click', function() {
  var thisEl = $('#contextParseWidget');
  if (!contextParseWidgetClicked) {
    thisEl.html('Done!');
    thisEl.css('backgroundColor', '#CCC');

    // For deploy/production please uncomment commented lines and remove my data variable, to be replaced with original
    var xmlhttp = new XMLHttpRequest();
    var url = "http://localhost:8080/";

     xmlhttp.onreadystatechange = function() {
       if (this.readyState == 4 && this.status == 200) {
        // ORIGINAL DATA VARIABLE
         var data = this.responseText;

        try {
          newData = JSON.parse(data);
          } catch (e) {
            // You can read e for more info
            // Let's assume the error is that we already have parsed the payload
            // So just return that
            newData = data;
        }

        var entities = newData.entities.entities;
        for (var i = 0; i < entities.length; i++)
        {
          $("p").filter(function() {
              return $(this).html().includes(entities[i].name);
          }).each(function(index) {
            var changedText = $(this).html().replace(entities[i].name, '<span class="'+entities[i].tag+'">'+entities[i].name+'</span>');
              $(this).html(changedText);
          });
        }
        document.querySelectorAll('[class^="entity"]').forEach(entity => entity.addEventListener('click', entityClickHandler));
      }
     };
     xmlhttp.open("POST", url, true);
     xmlhttp.send(document.getElementsByClassName("content-container")[0].innerHTML);
    contextParseWidgetClicked = true;
  }
});

// $('body').on('click', '[class^=entity]', function() {
function entityClickHandler() {
  if(entityClicked) {
    if($('#widgetBox').hasClass('show')) {
      $('#widgetBox').removeClass('show');
    }
    entityClicked = false;
    return;
  }
  var currentEntity = $(this).attr('class');
  var entities = newData.entities.entities;
  var htmlContent = '';
  for (var i = 0; i < entities.length; i++)
  {
    if(currentEntity === entities[i].tag)
    {
      htmlContent += '<h2>' + entities[i].name + '</h2><h3 class="subtitle">Summary</h3><p class="body desc">' + entities[i].description + "</p>";

      if(entities[i]["eLife Links"])
      {
        htmlContent += '<hr/><h3 class="title">Related Links</h3>'
        for(var j = 0; j < entities[i]["eLife Links"].length; j++)
        {
          htmlContent += '<a class="link" target="_blank" href="' + entities[i]["eLife Links"][j].link + '">' + entities[i]["eLife Links"][j].title + '</a><br/>';
        }
      }

      $('#widgetBox').html(htmlContent);
      if($('#widgetBox').hasClass('show') == false) {
        $('#widgetBox').addClass('show');
      }
    }
  }
  entityClicked = true;
}
