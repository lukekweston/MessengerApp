﻿namespace MessengerAPI.Models
{
    public class User
    {
        public int Id { get; set; } 
        public string UserName { get; set; }
        public string UserEmail { get; set; }
        public string Password { get; set; }

        public virtual ICollection<UserConversation> UserConversations { get; set; }
    }
}