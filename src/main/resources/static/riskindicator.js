// Copyright (C) 2013 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

Gerrit.install(function(self) {
    function onOpenChange(c, r) {
      var url = "changes/"
          + c._number
          + "/revisions/"
          + r._number
          + "/"
          + self.getPluginName()
          + "~"
          + "riskindicator";
      var changeTable = document
          .getElementById('change_infoTable');
      var parent = changeTable.parentNode;
        var insertPoint = changeTable.nextSibling;
      Gerrit.get(url, function(r) {
         var doc = document;
         var frg = doc.createDocumentFragment();
         var hr = doc.createElement('hr');
          frg.appendChild(hr);
          var table = doc.createElement('table');
          table.style.borderSpacing = 0;
          table.style.width = '100%';
          table.style.marginLeft = '2px';
          table.style.marginRight = '5px';
          var tableBody = doc.createElement('tbody');
         for (var i = 0; i < r.length; i++) {
           // row
           var tr = doc.createElement('tr');

           // greet
           var ri = r[i];
           // first column: type
           var td = doc.createElement('th');
             td.style.verticalAlign = 'middle';
             td.style.width = 'auto';
           td.appendChild(doc.createTextNode(ri.type));
           tr.appendChild(td);
           // second column: risk
           td = doc.createElement('td');
           var img = doc.createElement('img');
             img.style.float = 'right';
           img.src = ri.image;
           img.alt= ri.level;
           td.appendChild(img);
           tr.appendChild(td);
             tableBody.appendChild(tr);
         }
          table.appendChild(tableBody);
          frg.appendChild(table);
          parent.insertBefore(frg, insertPoint);

      });
    }
    Gerrit.on('showchange', onOpenChange);
  });
