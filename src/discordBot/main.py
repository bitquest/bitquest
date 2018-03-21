#/usr/bin/env python3
#This is the discord bot bitquest
#it require python3.6 and discord.py lib
#!/usr/bin/env python3
import discord
from discord.ext.commands import Bot
import asyncio

client = Bot(command_prefix="$")

@client.event
async def on_ready():
	print('Logged in as ' + client.user.name + ' ' + client.user.id)

@client.command(pass_context = True)
async def ping(e):
	"""pong"""
  print(str(e.message.author.name) + " make a ping")
	await client.send_message(e.message.channel, "pong")

client.run('YOUR KEY HERE')
