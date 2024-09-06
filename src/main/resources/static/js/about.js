console.log("about.js Yüklendi.")
// Sayfa yüklendiğinde click eventini bağlayalım
var aboutButton = document.getElementById("aboutButton");

aboutButton.addEventListener("click", function () {
     AlertMessage.confirmMessage("Butona tıkladınız. Devam etmek ister misiniz?",(confirmed)=>{
          if(confirmed){
               console.log("devam ettiniz.")
          }
          else{
               console.log("işlem reddedildi.");
          }
     });
});