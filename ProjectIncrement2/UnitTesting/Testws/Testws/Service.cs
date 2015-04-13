using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net;
using NUnit.Framework;
using System.IO;
namespace Testws
{
    [TestFixture]
    public class Service
    {
        [Test]
        public void restGetImages()
        {
            //ASCIIEncoding encoder = new ASCIIEncoding();
            //byte[] data = encoder.GetBytes(serializedObject);
            WebRequest req = WebRequest.Create(@"http://lasir.umkc.edu:8080/greengardenservice/webresources/ggarden/itemslist?start=0&limit=1");
            req.Method = "GET";
            req.ContentType = "application/json";
            req.Method = "GET";
            //req.ContentLength = 0;
            HttpWebResponse resp = req.GetResponse() as HttpWebResponse;
            if (resp.StatusCode == HttpStatusCode.OK)
            {
                using (Stream respStream = resp.GetResponseStream())
                {
                    StreamReader reader = new StreamReader(respStream, Encoding.UTF8);
                    Assert.AreEqual(HttpStatusCode.OK, resp.StatusCode);
                }
            }
        }

        [Test]
        public void restGetImageIds()
        {
            //ASCIIEncoding encoder = new ASCIIEncoding();
            //byte[] data = encoder.GetBytes(serializedObject);
            WebRequest req = WebRequest.Create(@"http://lasir.umkc.edu:8080/greengarden/webresources/ggarden/sceneids?start=0&limit=1");
            req.Method = "GET";
            req.ContentType = "application/json";
            //req.Method = "POST";
            //req.ContentLength = 0;
            HttpWebResponse resp = req.GetResponse() as HttpWebResponse;
            if (resp.StatusCode == HttpStatusCode.OK)
            {
                using (Stream respStream = resp.GetResponseStream())
                {
                    StreamReader reader = new StreamReader(respStream, Encoding.UTF8);
                    Assert.AreEqual("54f3eed2e4b0ceb891145128", reader.ReadToEnd());
                }
            }
        }

        [Test]
        public void postObserverData()
        {
            WebRequest req = WebRequest.Create(@"http://lasir.umkc.edu:8080/greengarden/webresources/ggarden/sceneids?start=0&limit=1");
            req.Method = "GET";
            req.ContentType = "application/json";
            //req.Method = "POST";
            //req.ContentLength = 0;
            HttpWebResponse resp = req.GetResponse() as HttpWebResponse;
            if (resp.StatusCode == HttpStatusCode.OK)
            {
                using (Stream respStream = resp.GetResponseStream())
                {
                    StreamReader reader = new StreamReader(respStream, Encoding.UTF8);
                    Assert.AreEqual("54f3eed2e4b0ceb891145128", reader.ReadToEnd());
                }
            }
        }
    }
}
