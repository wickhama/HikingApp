const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();

const nodemailer = require('nodemailer');
const gmailEmail = functions.config().gmail.email;
const gmailPassword = functions.config().gmail.password;
const mailTransport = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: gmailEmail,
    pass: gmailPassword,
  },
});

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.addMessage = functions.https.onRequest((request, response) => {
//     const original = request.query.text;
//     
//     return admin.database().ref('/messages').push({original: original})
//                 .then((snapshot) => {
//                     return response.redirect(303, snapshot.ref.toString());
//                 });
// });
// 
// exports.makeUppercase = functions.database.ref('/messages/{pushId}/original')
//     .onCreate((snapshot, context) => {
//         const original = snapshot.val();
//         console.log('Uppercasing', context.params.pushId, original);
//         const uppercase = original.toUpperCase();
//         
//         return snapshot.ref.parent.child('uppercase').set(uppercase);
//     });
    
exports.onTrailFlagged = functions.database.ref('/Trails/{trailId}/metadata/numFlags')
    .onUpdate((event, context) => {
        const oldVal = event.before.val();
        const newVal = event.after.val();
        const trailId = context.params.trailId;
        
        if(newVal <= oldVal) {
            return null;
        }
        
        const mailOptions = {
            from: '"ArcTrails Database" <noreply@firebase.com>',
            to:gmailEmail,
            subject:'"'+trailId+'" has been flagged '+newVal+' time(s)',
            text:'The trail at "'+trailId+'" currently has '+newVal+' flags.\n'+
                 'https://console.firebase.google.com/project/arctrails-b1a84/'+
                 'database/arctrails-b1a84/data/Trails/'+trailId.split(" ").join("%20"),
        }
        
        return mailTransport.sendMail(mailOptions)
            .then(() => console.log('Trail flagged', trailId, newVal))
            .catch((error) => console.error('There was an error while sending the email:',error));
    });
