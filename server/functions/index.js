'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');
const secureCompare = require('secure-compare');
admin.initializeApp();

/**
 * When requested this Function will create the trending posts
 * The request needs to be authorized by passing a 'key' query parameter in the URL. This key must
 * match a key set as an environment variable using `firebase functions:config:set cron.key="YOUR_KEY"`.
 */
exports.updatetrending = functions.https.onRequest(async (req, res) => {
  const key = req.query.key;

  // Exit if the keys don't match.
  if (!secureCompare(key, functions.config().cron.key)) {
    console.log('The key provided in the request does not match the key set in the environment. Check that', key,
        'matches the cron.key attribute in `firebase env:get`');
    res.status(403).send('Security key does not match. Make sure your "key" URL query parameter matches the ' +
        'cron.key environment variable.');
    return null;
  }
  
  // Fetch all user details.
  getMonthlyPosts();
  
  res.send('Trending posts updated');
  
  return null;
});

/**
 * Returns the list of all the sins in the last month.
 */
async function getMonthlyPosts(users = [], nextPageToken) 
{
  var db = admin.database();
  const sinsDB = db.ref('/sins').orderByChild("sinTime/timestamp");
  const trendingDB = db.ref('/trending');
  
  // Clear trending
  trendingDB.remove();
  
  const sins = sinsDB.once("value").then(function(snapshot) 
  {    
    snapshot.forEach(function(sins){
        // Calculate weight for each sin 
        var timeDiff = Date.now() - sins.child("sinTime/timestamp").val();
        var score = sins.child("comments").val() * 0.5 + sins.child("likes").val() * 0.35 + (1.0 / (timeDiff + 1.0)) * 0.15;
        console.log("Score: ", score);
        console.log("TimeDiff: ", timeDiff);
        
        // Update trending sins
        var newTrendingRef = trendingDB.push();
        newTrendingRef.set({
            key: sins.key,
            score: score
          });
    });

  });
  
  return users;
}

exports.storeLikedSin = functions.database.ref('/slikes/{sin_id}/{user_id}')
    .onWrite((snapshot, context) => {
      var db = admin.database();
      const beforeData = snapshot.before.val(); // data before the write
      const afterData = snapshot.after.val(); // data after the write
      if(beforeData != afterData)
      {
        if(afterData)
        {
            db.ref('/sins/' + context.params.sin_id + "/likes/").transaction(function(likes) {
            likes++;
            return likes;
            });
        }
        else
        {
            db.ref('/sins/' + context.params.sin_id + "/likes/").transaction(function(likes) {
            likes--;
            return likes;
            });
       }
      }
      return 0;
    });
    
exports.storeLikedComment = functions.database.ref('/clikes/{sin_id}/{comment_id}/{user_id}')
    .onWrite((snapshot, context) => {
      var db = admin.database();
      const beforeData = snapshot.before.val(); // data before the write
      const afterData = snapshot.after.val(); // data after the write
      
        console.log("before data: ", beforeData);
        console.log("afterData data: ", afterData);
      if(beforeData != afterData)
      {
        if(afterData)
        {
            db.ref('/comments/' + context.params.sin_id + "/" + context.params.comment_id + "/likes/").transaction(function(likes) {
            likes++;
            return likes;
            });
        }
        else
        {
            db.ref('/comments/' + context.params.sin_id + "/" + context.params.comment_id + "/likes/").transaction(function(likes) {
            likes--;
            return likes;
            });
       }
      }
      return 0;
    });