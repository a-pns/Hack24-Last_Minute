//Place script that manipulates document here
var div=document.createElement("div");
document.body.insertBefore(div, document.body.firstChild);
div.innerText='Contexualise';
div.id = 'contextParseWidget';

document.getElementById('contextParseWidget').addEventListener('click', function() {
  var xmlhttp = new XMLHttpRequest();
  var url = "http://localhost:8080/";

  xmlhttp.onreadystatechange = function() {
      if (this.readyState == 4 && this.status == 200) {
          var data = this.responseText;
          var newData;
          try {
            newData = JSON.parse(data);
            } catch (e) {
              // You can read e for more info
              // Let's assume the error is that we already have parsed the payload
              // So just return that
              newData = data;
          }
          console.log(newData);
          var entities = newData.entities.entities;
          for (var i = 0; i < entities.length; i++)
          {
            $("p").filter(function() {
                console.log($(this).html().includes(entities[i].name))
                return $(this).html().includes(entities[i].name);
            }).each(function( index ) {
              var changedText = $(this).html().replace(entities[i].name, '<span class="'+entities[i].tag+'">'+entities[i].name+'</span>');
                $(this).html(changedText);
            });
          }
      }
  };
  xmlhttp.open("POST", url, true);
  xmlhttp.send(document.getElementsByClassName("content-container")[0].innerHTML);
});
