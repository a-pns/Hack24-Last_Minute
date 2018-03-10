import app.views
from flask import Flask, request, session, g, redirect, url_for, abort, \
     render_template, flash
app = Flask(__name__)


@app.route('/', methods=["POST"])
def search_entities():
    return views.search_entities()
