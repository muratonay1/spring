
console.log("Projects.js yüklendi.");

document.getElementById('fetch-ip-btn').addEventListener('click', function () {
    Caller.call({
        method:"POST",
        endpoint:"auth/register",
        options:{
            body:{
                "email":"test@test.com.tr",
                "password":"123"
            }
        },
        done:(response)=>{
            console.log('Response:', error);
        },
        fail:(error)=>{
            AlertMessage.errorMessage(error);
            console.error('ERROR:', error);
        }
    })
});